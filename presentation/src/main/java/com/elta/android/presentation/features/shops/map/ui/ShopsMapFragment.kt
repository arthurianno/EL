package com.elta.android.presentation.features.shops.map.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.elta.android.domain.features.sale_points.model.Type
import com.elta.android.presentation.R
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialog
import com.elta.android.presentation.core.compose.widgets.dialogs.BaseDialogWidgetModel
import com.elta.android.presentation.core.geo.GeoPoint
import com.elta.android.presentation.core.permissions.requestStatus
import com.elta.android.presentation.core.permissions.statusFor
import com.elta.android.presentation.core.pm.widgets.bindTo
import com.elta.android.presentation.core.pm.widgets.resolveResults
import com.elta.android.presentation.core.ui.fragment.BaseYandexMapFragment
import com.elta.android.presentation.core.ui.fragment.addOnBackPressedCallback
import com.elta.android.presentation.core.ui.system_ui.StatusBarConfigProvider
import com.elta.android.presentation.core.ui.system_ui.TransparentStatusBarConfigProvider
import com.elta.android.presentation.databinding.FragmentShopsMapBinding
import com.elta.android.presentation.features.shops.map.pm.ShopsMapPm
import com.elta.android.presentation.features.shops.map.ui.adapter.MapAdapter
import com.elta.android.presentation.features.shops.map.ui.widgets.ShopClusterPinProvider
import com.elta.android.presentation.utils.applyWindowInsetsForChildrenView
import com.elta.android.presentation.utils.bundle
import com.elta.android.presentation.utils.openSettingsIntent
import com.elta.android.presentation.utils.pageScrolled
import com.elta.android.presentation.utils.scrollSmooth
import com.elta.android.presentation.utils.scrollStateChanges
import com.elta.android.presentation.utils.setEmojiFilter
import com.elta.android.presentation.utils.toPoint
import com.elta.android.presentation.widgets.FixedLinearLayoutManager
import com.elta.android.presentation.widgets.decoration.MarginItemDecoration
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.text
import com.jakewharton.rxbinding2.widget.textChanges
import com.nullgr.core.adapter.items.ListItem
import com.nullgr.core.ui.extensions.hideKeyboard
import com.tbruyelle.rxpermissions2.RxPermissions
import javax.inject.Inject
import me.dmdev.rxpm.bindTo
import me.dmdev.rxpm.widget.bindTo

@Suppress("MagicNumber")
class ShopsMapFragment :
    BaseYandexMapFragment<ShopsMapPm, FragmentShopsMapBinding>(FragmentShopsMapBinding::inflate) {

    @Inject
    lateinit var mapAdapter: MapAdapter

    @Inject
    lateinit var mapSearchAdapter: MapAdapter

    lateinit var locationDialog: BaseDialogWidgetModel<Nothing>

    override val searchAdapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { mapSearchAdapter }
    override val adapter: ListAdapter<ListItem, RecyclerView.ViewHolder> by lazy { mapAdapter }
    override val statusBarConfigProvider: StatusBarConfigProvider =
        TransparentStatusBarConfigProvider
    override val screenLayout: Int = R.layout.fragment_shops_map
    override val classToken: Class<ShopsMapPm> = ShopsMapPm::class.java

    override val userLocationPinRes = R.drawable.ic_my_loc
    override val clusterPinProvider by lazy(LazyThreadSafetyMode.NONE) {
        ShopClusterPinProvider(requireActivity())
    }

    private val snapHelper = PagerSnapHelper()

    private val rxPermissions by lazy { RxPermissions(this) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        addOnBackPressedCallback {
            presentationModel.skipAction.consumer.accept(Unit)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (arguments?.get(EXTRA_TYPE) as? Type)?.let { presentationModel.setShopsType(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.toolbar) {
            homeButtonView.setImageResource(R.drawable.ic_dialog_close)
            toolbarView.applyWindowInsetsForChildrenView()
        }
        with(binding) {
            locationDialogInit()
            locationPermissionDialog.setContent {
                BaseDialog(widgetModel = locationDialog)
            }

            itemsView.layoutManager =
                FixedLinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL)
            itemsView.adapter = adapter
            itemsView.addItemDecoration(
                MarginItemDecoration(
                    requireContext(),
                    R.dimen.shop_margin,
                    R.dimen.shop_margin,
                    R.dimen.shop_between
                )
            )
            snapHelper.attachToRecyclerView(itemsView)

            searchItemsView.layoutManager = FixedLinearLayoutManager(requireContext())
            searchItemsView.adapter = searchAdapter
            searchInputView.setEmojiFilter()
        }
    }

    private fun locationDialogInit() {
        locationDialog = BaseDialogWidgetModel(
            positiveOnCLick = { presentationModel.openSettingsAction.consumer.accept(Unit) }
        )
        locationDialog.initDialog(
            title = getString(R.string.settings_dialog_title),
            message = getString(R.string.map_location_dialog_message),
            positiveButtonText = getString(R.string.settings_dialog_positive),
            negativeButtonText = getString(R.string.settings_dialog_negative)
        )
    }

    override fun onBindPresentationModel(pm: ShopsMapPm) {
        super.onBindPresentationModel(pm)
        binding.myLocationButtonView.clicks().bindTo(pm.moveToMyLocationAction)
        binding.toolbar.homeButtonView.clicks().bindTo(pm.skipAction)
        pm.titleState.bindTo(binding.toolbar.toolbarTitleView.text())
        // due to large data set diff utils works with issues or
        // requires a lot of resources for calculation, so disable it.
        pm.items.bindTo { adapter.submitList(it) }
        pm.addMyLocationPinCommand.bindTo(::showUserLocation)
        pm.showDefaultScreenStateCommand.bindTo(::moveToPointsInBounds)
        pm.navigateToLocationCommand.bindTo { moveTo(it.location.toPoint(), zoom = it.zoom) }

        pm.openSettingsCommand.bindTo { openSettingsIntent(requireContext()) }

        pm.checkPermissionStatusCommand.bindTo {
            val status = rxPermissions.statusFor(LOCATION_PERMISSION)
            pm.setPermissionStatus(status)
        }
        pm.showLocationPermissionDialog.bindTo { locationDialog.dialogOpen() }

        pm.requestPermissionCommand.observable
            .flatMap { rxPermissions.requestStatus(LOCATION_PERMISSION) }
            .subscribe(pm::setPermissionStatus)

        pm.geoPoints.bindTo { (points: List<GeoPoint>, selected: Int) ->
            addPins(points)
            selectPin(points[selected], false)
        }

        pm.selectGeoPointCommand.bindTo { selectPin(it, true) }
        pm.selectShopItemCommand.bindTo(binding.itemsView::scrollSmooth)

        pinClicks().skip(1).bindTo(pm.shopItemGeoPointSelectedAction)
        binding.itemsView.pageScrolled().bindTo(pm.shopListItemSelectedAction)

        // search
        pm.searchHintState.bindTo(binding.searchInputView::setHint)
        pm.searchItems.bindTo { searchAdapter.submitList(it) }
        pm.searchInput.bindTo(binding.searchInputView)
        pm.searchCloseCommand.bindTo {
            binding.searchInputView.hideKeyboard()
            activity?.window?.decorView?.clearFocus()
        }
        binding.searchClearView.clicks().bindTo(pm.searchClearAction)
        binding.searchInputView.textChanges().map { it.isNotEmpty() }
            .subscribe { binding.searchIconView.isSelected = it }
        binding.searchItemsView.scrollStateChanges()
            .filter { it != RecyclerView.SCROLL_STATE_IDLE }
            .subscribe { binding.searchInputView.hideKeyboard() }

        pm.locationControl.bindTo(compositeUnbind, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presentationModel.locationControl.resolveResults(requestCode, resultCode)
    }

    private fun showUserLocation(location: Location) {
        addMyLocationPin(location)
    }

    companion object {

        fun newInstance(type: Type) = ShopsMapFragment().apply {
            arguments = bundle(EXTRA_TYPE to type)
        }

        private const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val EXTRA_TYPE = "extra_shops_type"
    }
}
