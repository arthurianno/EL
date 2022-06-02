package com.elta.android.presentation.core.ui.bottom_sheet
//
// import android.content.Context
// import android.util.AttributeSet
// import android.util.TypedValue
// import android.view.View
// import androidx.annotation.VisibleForTesting
// import androidx.coordinatorlayout.widget.CoordinatorLayout
// import java.lang.IllegalArgumentException
// import java.lang.annotation.Retention
// import java.lang.annotation.RetentionPolicy
// import java.lang.ref.WeakReference
// import java.util.HashMap
//
// /**
// * Default BottomSheetBehavior from android.support.design.widget
// */
// class _BottomSheetBehavior<V : View?> : CoordinatorLayout.Behavior<V> {
//    private var fitToContents = true
//    private var maximumVelocity = 0f
//    private var peekHeight = 0
//    private var peekHeightAuto = false
//
//    @get:VisibleForTesting
//    var peekHeightMin = 0
//        private set
//    private var lastPeekHeight = 0
//    var fitToContentsOffset = 0
//    var halfExpandedOffset = 0
//    var collapsedOffset = 0
//    var isHideable = false
//    var skipCollapsed = false
//    var state = 4
//    var viewDragHelper: ViewDragHelper? = null
//    private var ignoreEvents = false
//    private var lastNestedScrollDy = 0
//    private var nestedScrolled = false
//    var parentHeight = 0
//    var viewRef: WeakReference<V>? = null
//    var nestedScrollingChildRef: WeakReference<View?>? = null
//    private var callback: BottomSheetCallback? = null
//    private var velocityTracker: VelocityTracker? = null
//    var activePointerId = 0
//    private var initialY = 0
//    var touchingScrollingChild = false
//    private var importantForAccessibilityMap: MutableMap<View?, Int?>? = null
//    private val dragCallback: Callback
//
//    constructor() {
//        dragCallback = NamelessClass_1()
//    }
//
//    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
//        dragCallback = NamelessClass_1()
//        val a: TypedArray =
//            context.obtainStyledAttributes(attrs, styleable.BottomSheetBehavior_Layout)
//        val value: TypedValue =
//            a.peekValue(styleable.BottomSheetBehavior_Layout_behavior_peekHeight)
//        if (value != null && value.data == -1) {
//            setPeekHeight(value.data)
//        } else {
//            setPeekHeight(
//                a.getDimensionPixelSize(
//                    styleable.BottomSheetBehavior_Layout_behavior_peekHeight,
//                    -1
//                )
//            )
//        }
//        isHideable = a.getBoolean(styleable.BottomSheetBehavior_Layout_behavior_hideable, false)
//        setFitToContents(
//            a.getBoolean(
//                styleable.BottomSheetBehavior_Layout_behavior_fitToContents,
//                true
//            )
//        )
//        skipCollapsed =
//            a.getBoolean(styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false)
//        a.recycle()
//        val configuration: ViewConfiguration = ViewConfiguration.get(context)
//        maximumVelocity = configuration.getScaledMaximumFlingVelocity().toFloat()
//    }
//
//    internal inner class NamelessClass_1 : Callback() {
//        fun tryCaptureView(@NonNull child: View, pointerId: Int): Boolean {
//            return if (state == 1) {
//                false
//            } else if (touchingScrollingChild) {
//                false
//            } else {
//                if (state == 3 && activePointerId == pointerId) {
//                    val scroll =
//                        nestedScrollingChildRef!!.get()
//                    if (scroll != null && scroll.canScrollVertically(-1)) {
//                        return false
//                    }
//                }
//                viewRef != null && viewRef!!.get() === child
//            }
//        }
//
//        fun onViewPositionChanged(
//            @NonNull changedView: View?,
//            left: Int,
//            top: Int,
//            dx: Int,
//            dy: Int
//        ) {
//            dispatchOnSlide(top)
//        }
//
//        fun onViewDragStateChanged(state: Int) {
//            if (state == 1) {
//                setStateInternal(1)
//            }
//        }
//
//        fun onViewReleased(@NonNull releasedChild: View, xvel: Float, yvel: Float) {
//            val top: Int
//            val targetState: Byte
//            val currentTop: Int
//            if (yvel < 0.0f) {
//                if (fitToContents) {
//                    top = fitToContentsOffset
//                    targetState = 3
//                } else {
//                    currentTop = releasedChild.top
//                    if (currentTop > halfExpandedOffset) {
//                        top = halfExpandedOffset
//                        targetState = 6
//                    } else {
//                        top = 0
//                        targetState = 3
//                    }
//                }
//            } else if (!isHideable || !shouldHide(
//                    releasedChild,
//                    yvel
//                ) || releasedChild.top <= collapsedOffset && Math.abs(xvel) >= Math.abs(yvel)
//            ) {
//                if (yvel != 0.0f && Math.abs(xvel) <= Math.abs(yvel)) {
//                    top = collapsedOffset
//                    targetState = 4
//                } else {
//                    currentTop = releasedChild.top
//                    if (fitToContents) {
//                        if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
//                            top = fitToContentsOffset
//                            targetState = 3
//                        } else {
//                            top = collapsedOffset
//                            targetState = 4
//                        }
//                    } else if (currentTop < halfExpandedOffset) {
//                        if (currentTop < Math.abs(currentTop - collapsedOffset)) {
//                            top = 0
//                            targetState = 3
//                        } else {
//                            top = halfExpandedOffset
//                            targetState = 6
//                        }
//                    } else if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
//                        top = halfExpandedOffset
//                        targetState = 6
//                    } else {
//                        top = collapsedOffset
//                        targetState = 4
//                    }
//                }
//            } else {
//                top = parentHeight
//                targetState = 5
//            }
//            if (viewDragHelper.settleCapturedViewAt(releasedChild.left, top)) {
//                setStateInternal(2)
//                ViewCompat.postOnAnimation(
//                    releasedChild,
//                    this@BottomSheetBehavior.SettleRunnable(releasedChild, targetState.toInt())
//                )
//            } else {
//                setStateInternal(targetState.toInt())
//            }
//        }
//
//        fun clampViewPositionVertical(@NonNull child: View?, top: Int, dy: Int): Int {
//            return MathUtils.clamp(
//                top,
//                expandedOffset,
//                if (isHideable) parentHeight else collapsedOffset
//            )
//        }
//
//        fun clampViewPositionHorizontal(@NonNull child: View, left: Int, dx: Int): Int {
//            return child.left
//        }
//
//        fun getViewVerticalDragRange(@NonNull child: View?): Int {
//            return if (isHideable) parentHeight else collapsedOffset
//        }
//    }
//
//    fun onSaveInstanceState(parent: CoordinatorLayout?, child: V): Parcelable {
//        return SavedState(super.onSaveInstanceState(parent, child), state)
//    }
//
//    fun onRestoreInstanceState(parent: CoordinatorLayout?, child: V, state: Parcelable) {
//        val ss = state as SavedState
//        super.onRestoreInstanceState(parent, child, ss.getSuperState())
//        if (ss.state != 1 && ss.state != 2) {
//            this.state = ss.state
//        } else {
//            this.state = 4
//        }
//    }
//
//    fun onLayoutChild(parent: CoordinatorLayout, child: V, layoutDirection: Int): Boolean {
//        if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
//            child!!.fitsSystemWindows = true
//        }
//        val savedTop = child!!.top
//        parent.onLayoutChild(child, layoutDirection)
//        parentHeight = parent.getHeight()
//        if (peekHeightAuto) {
//            if (peekHeightMin == 0) {
//                peekHeightMin = parent.getResources()
//                    .getDimensionPixelSize(dimen.design_bottom_sheet_peek_height_min)
//            }
//            lastPeekHeight = Math.max(peekHeightMin, parentHeight - parent.getWidth() * 9 / 16)
//        } else {
//            lastPeekHeight = peekHeight
//        }
//        fitToContentsOffset = Math.max(0, parentHeight - child.height)
//        halfExpandedOffset = parentHeight / 2
//        calculateCollapsedOffset()
//        if (state == 3) {
//            ViewCompat.offsetTopAndBottom(child, expandedOffset)
//        } else if (state == 6) {
//            ViewCompat.offsetTopAndBottom(child, halfExpandedOffset)
//        } else if (isHideable && state == 5) {
//            ViewCompat.offsetTopAndBottom(child, parentHeight)
//        } else if (state == 4) {
//            ViewCompat.offsetTopAndBottom(child, collapsedOffset)
//        } else if (state == 1 || state == 2) {
//            ViewCompat.offsetTopAndBottom(child, savedTop - child.top)
//        }
//        if (viewDragHelper == null) {
//            viewDragHelper = ViewDragHelper.create(parent, dragCallback)
//        }
//        viewRef = WeakReference<Any?>(child)
//        nestedScrollingChildRef = WeakReference<Any?>(findScrollingChild(child))
//        return true
//    }
//
//    fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
//        return if (!child!!.isShown) {
//            ignoreEvents = true
//            false
//        } else {
//            val action: Int = event.getActionMasked()
//            if (action == 0) {
//                reset()
//            }
//            if (velocityTracker == null) {
//                velocityTracker = VelocityTracker.obtain()
//            }
//            velocityTracker.addMovement(event)
//            when (action) {
//                0 -> {
//                    val initialX: Int = event.getX().toInt()
//                    initialY = event.getY().toInt()
//                    val scroll =
//                        if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
//                    if (scroll != null && parent.isPointInChildBounds(
//                            scroll,
//                            initialX,
//                            initialY
//                        )
//                    ) {
//                        activePointerId = event.getPointerId(event.getActionIndex())
//                        touchingScrollingChild = true
//                    }
//                    ignoreEvents = activePointerId == -1 && !parent.isPointInChildBounds(
//                        child,
//                        initialX,
//                        initialY
//                    )
//                }
//                1, 3 -> {
//                    touchingScrollingChild = false
//                    activePointerId = -1
//                    if (ignoreEvents) {
//                        ignoreEvents = false
//                        return false
//                    }
//                }
//                2 -> {}
//            }
//            if (!ignoreEvents && viewDragHelper != null && viewDragHelper.shouldInterceptTouchEvent(
//                    event
//                )
//            ) {
//                true
//            } else {
//                val scroll =
//                    if (nestedScrollingChildRef != null) nestedScrollingChildRef!!.get() else null
//                action == 2 && scroll != null && !ignoreEvents && state != 1 && !parent.isPointInChildBounds(
//                    scroll,
//                    event.getX().toInt(),
//                    event.getY().toInt()
//                ) && viewDragHelper != null && Math.abs(initialY.toFloat() - event.getY()) > viewDragHelper.getTouchSlop() as Float
//            }
//        }
//    }
//
//    fun onTouchEvent(parent: CoordinatorLayout?, child: V, event: MotionEvent): Boolean {
//        return if (!child!!.isShown) {
//            false
//        } else {
//            val action: Int = event.getActionMasked()
//            if (state == 1 && action == 0) {
//                true
//            } else {
//                if (viewDragHelper != null) {
//                    viewDragHelper.processTouchEvent(event)
//                }
//                if (action == 0) {
//                    reset()
//                }
//                if (velocityTracker == null) {
//                    velocityTracker = VelocityTracker.obtain()
//                }
//                velocityTracker.addMovement(event)
//                if (action == 2 && !ignoreEvents && Math.abs(initialY.toFloat() - event.getY()) > viewDragHelper.getTouchSlop() as Float) {
//                    viewDragHelper.captureChildView(
//                        child,
//                        event.getPointerId(event.getActionIndex())
//                    )
//                }
//                !ignoreEvents
//            }
//        }
//    }
//
//    fun onStartNestedScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull directTargetChild: View?,
//        @NonNull target: View?,
//        axes: Int,
//        type: Int
//    ): Boolean {
//        lastNestedScrollDy = 0
//        nestedScrolled = false
//        return axes and 2 != 0
//    }
//
//    fun onNestedPreScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View,
//        dx: Int,
//        dy: Int,
//        @NonNull consumed: IntArray,
//        type: Int
//    ) {
//        if (type != 1) {
//            if (target === nestedScrollingChildRef!!.get()) {
//                val currentTop = child!!.top
//                val newTop = currentTop - dy
//                if (dy > 0) {
//                    if (newTop < expandedOffset) {
//                        consumed[1] = currentTop - expandedOffset
//                        ViewCompat.offsetTopAndBottom(child, -consumed[1])
//                        setStateInternal(3)
//                    } else {
//                        consumed[1] = dy
//                        ViewCompat.offsetTopAndBottom(child, -dy)
//                        setStateInternal(1)
//                    }
//                } else if (dy < 0 && !target.canScrollVertically(-1)) {
//                    if (newTop > collapsedOffset && !isHideable) {
//                        consumed[1] = currentTop - collapsedOffset
//                        ViewCompat.offsetTopAndBottom(child, -consumed[1])
//                        setStateInternal(4)
//                    } else {
//                        /*
//                          FIX
//
//                          When clicked, it scrolls 1-2 pixels
//                          and considers that it is necessary to close the dialog
//                         */
//                        if (dy > -15) return
//                        consumed[1] = dy
//                        ViewCompat.offsetTopAndBottom(child, -dy)
//                        setStateInternal(1)
//                    }
//                }
//                dispatchOnSlide(child.top)
//                lastNestedScrollDy = dy
//                nestedScrolled = true
//            }
//        }
//    }
//
//    fun onStopNestedScroll(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View,
//        type: Int
//    ) {
//        if (child!!.top == expandedOffset) {
//            setStateInternal(3)
//        } else if (target === nestedScrollingChildRef!!.get() && nestedScrolled) {
//            val top: Int
//            val targetState: Byte
//            if (lastNestedScrollDy > 0) {
//                top = expandedOffset
//                targetState = 3
//            } else if (isHideable && shouldHide(child, yVelocity)) {
//                top = parentHeight
//                targetState = 5
//            } else if (lastNestedScrollDy == 0) {
//                val currentTop = child.top
//                if (fitToContents) {
//                    if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
//                        top = fitToContentsOffset
//                        targetState = 3
//                    } else {
//                        top = collapsedOffset
//                        targetState = 4
//                    }
//                } else if (currentTop < halfExpandedOffset) {
//                    if (currentTop < Math.abs(currentTop - collapsedOffset)) {
//                        top = 0
//                        targetState = 3
//                    } else {
//                        top = halfExpandedOffset
//                        targetState = 6
//                    }
//                } else if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
//                    top = halfExpandedOffset
//                    targetState = 6
//                } else {
//                    top = collapsedOffset
//                    targetState = 4
//                }
//            } else {
//                top = collapsedOffset
//                targetState = 4
//            }
//            if (viewDragHelper.smoothSlideViewTo(child, child.left, top)) {
//                setStateInternal(2)
//                ViewCompat.postOnAnimation(child, SettleRunnable(child, targetState))
//            } else {
//                setStateInternal(targetState.toInt())
//            }
//            nestedScrolled = false
//        }
//    }
//
//    fun onNestedPreFling(
//        @NonNull coordinatorLayout: CoordinatorLayout?,
//        @NonNull child: V,
//        @NonNull target: View,
//        velocityX: Float,
//        velocityY: Float
//    ): Boolean {
//        return target === nestedScrollingChildRef!!.get() && (state != 3 || super.onNestedPreFling(
//            coordinatorLayout,
//            child,
//            target,
//            velocityX,
//            velocityY
//        ))
//    }
//
//    fun isFitToContents(): Boolean {
//        return fitToContents
//    }
//
//    fun setFitToContents(fitToContents: Boolean) {
//        if (this.fitToContents != fitToContents) {
//            this.fitToContents = fitToContents
//            if (viewRef != null) {
//                calculateCollapsedOffset()
//            }
//            setStateInternal(if (this.fitToContents && state == 6) 3 else state)
//        }
//    }
//
//    fun setPeekHeight(peekHeight: Int) {
//        var layout = false
//        if (peekHeight == -1) {
//            if (!peekHeightAuto) {
//                peekHeightAuto = true
//                layout = true
//            }
//        } else if (peekHeightAuto || this.peekHeight != peekHeight) {
//            peekHeightAuto = false
//            this.peekHeight = Math.max(0, peekHeight)
//            collapsedOffset = parentHeight - peekHeight
//            layout = true
//        }
//        if (layout && state == 4 && viewRef != null) {
//            val view = viewRef!!.get()
//            view?.requestLayout()
//        }
//    }
//
//    fun getPeekHeight(): Int {
//        return if (peekHeightAuto) -1 else peekHeight
//    }
//
//    fun setBottomSheetCallback(callback: BottomSheetCallback?) {
//        this.callback = callback
//    }
//
//    fun setState(state: Int) {
//        if (state != this.state) {
//            if (viewRef == null) {
//                if (state == 4 || state == 3 || state == 6 || isHideable && state == 5) {
//                    this.state = state
//                }
//            } else {
//                val child = viewRef!!.get()
//                if (child != null) {
//                    val parent: ViewParent? = child.parent
//                    if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(
//                            child
//                        )
//                    ) {
//                        child.post(Runnable { startSettlingAnimation(child, state) })
//                    } else {
//                        startSettlingAnimation(child, state)
//                    }
//                }
//            }
//        }
//    }
//
//    fun getState(): Int {
//        return state
//    }
//
//    fun setStateInternal(state: Int) {
//        if (this.state != state) {
//            this.state = state
//            if (state != 6 && state != 3) {
//                if (state == 5 || state == 4) {
//                    updateImportantForAccessibility(false)
//                }
//            } else {
//                updateImportantForAccessibility(true)
//            }
//            val bottomSheet = viewRef!!.get() as View?
//            if (bottomSheet != null && callback != null) {
//                callback!!.onStateChanged(bottomSheet, state)
//            }
//        }
//    }
//
//    private fun calculateCollapsedOffset() {
//        if (fitToContents) {
//            collapsedOffset = Math.max(parentHeight - lastPeekHeight, fitToContentsOffset)
//        } else {
//            collapsedOffset = parentHeight - lastPeekHeight
//        }
//    }
//
//    private fun reset() {
//        activePointerId = -1
//        if (velocityTracker != null) {
//            velocityTracker.recycle()
//            velocityTracker = null
//        }
//    }
//
//    fun shouldHide(child: View, yvel: Float): Boolean {
//        return if (skipCollapsed) {
//            true
//        } else if (child.top < collapsedOffset) {
//            false
//        } else {
//            val newTop = child.top.toFloat() + yvel * 0.1f
//            Math.abs(newTop - collapsedOffset.toFloat()) / peekHeight.toFloat() > 0.5f
//        }
//    }
//
//    @VisibleForTesting
//    fun findScrollingChild(view: View): View? {
//        return if (ViewCompat.isNestedScrollingEnabled(view)) {
//            view
//        } else {
//            if (view is ViewGroup) {
//                val group: ViewGroup = view as ViewGroup
//                var i = 0
//                val count: Int = group.getChildCount()
//                while (i < count) {
//                    val scrollingChild = findScrollingChild(group.getChildAt(i))
//                    if (scrollingChild != null) {
//                        return scrollingChild
//                    }
//                    ++i
//                }
//            }
//            null
//        }
//    }
//
//    private val yVelocity: Float
//        private get() = if (velocityTracker == null) {
//            0.0f
//        } else {
//            velocityTracker.computeCurrentVelocity(1000, maximumVelocity)
//            velocityTracker.getYVelocity(activePointerId)
//        }
//    private val expandedOffset: Int
//        private get() = if (fitToContents) fitToContentsOffset else 0
//
//    fun startSettlingAnimation(child: View, state: Int) {
//        var state = state
//        var top: Int
//        if (state == 4) {
//            top = collapsedOffset
//        } else if (state == 6) {
//            top = halfExpandedOffset
//            if (fitToContents && top <= fitToContentsOffset) {
//                state = 3
//                top = fitToContentsOffset
//            }
//        } else if (state == 3) {
//            top = expandedOffset
//        } else {
//            require(!(!isHideable || state != 5)) { "Illegal state argument: $state" }
//            top = parentHeight
//        }
//        if (viewDragHelper.smoothSlideViewTo(child, child.left, top)) {
//            setStateInternal(2)
//            ViewCompat.postOnAnimation(child, SettleRunnable(child, state))
//        } else {
//            setStateInternal(state)
//        }
//    }
//
//    fun dispatchOnSlide(top: Int) {
//        val bottomSheet = viewRef!!.get() as View?
//        if (bottomSheet != null && callback != null) {
//            if (top > collapsedOffset) {
//                callback!!.onSlide(
//                    bottomSheet,
//                    (collapsedOffset - top).toFloat() / (parentHeight - collapsedOffset).toFloat()
//                )
//            } else {
//                callback!!.onSlide(
//                    bottomSheet,
//                    (collapsedOffset - top).toFloat() / (collapsedOffset - expandedOffset).toFloat()
//                )
//            }
//        }
//    }
//
//    private fun updateImportantForAccessibility(expanded: Boolean) {
//        if (viewRef != null) {
//            val viewParent: ViewParent = (viewRef!!.get() as View?)!!.parent
//            if (viewParent is CoordinatorLayout) {
//                val parent: CoordinatorLayout = viewParent as CoordinatorLayout
//                val childCount: Int = parent.getChildCount()
//                if (VERSION.SDK_INT >= 16 && expanded) {
//                    if (importantForAccessibilityMap != null) {
//                        return
//                    }
//                    importantForAccessibilityMap = HashMap<Any?, Any?>(childCount)
//                }
//                for (i in 0 until childCount) {
//                    val child: View = parent.getChildAt(i)
//                    if (child !== viewRef!!.get()) {
//                        if (!expanded) {
//                            if (importantForAccessibilityMap != null && importantForAccessibilityMap!!.containsKey(
//                                    child
//                                )
//                            ) {
//                                ViewCompat.setImportantForAccessibility(
//                                    child,
//                                    importantForAccessibilityMap!![child]
//                                )
//                            }
//                        } else {
//                            if (VERSION.SDK_INT >= 16) {
//                                importantForAccessibilityMap!![child] =
//                                    child.importantForAccessibility
//                            }
//                            ViewCompat.setImportantForAccessibility(child, 4)
//                        }
//                    }
//                }
//                if (!expanded) {
//                    importantForAccessibilityMap = null
//                }
//            }
//        }
//    }
//
//    protected class SavedState : AbsSavedState {
//        val state: Int
//
//        @JvmOverloads
//        constructor(source: Parcel, loader: ClassLoader? = null as ClassLoader?) : super(
//            source,
//            loader
//        ) {
//            state = source.readInt()
//        }
//
//        constructor(superState: Parcelable?, state: Int) : super(superState) {
//            this.state = state
//        }
//
//        fun writeToParcel(out: Parcel, flags: Int) {
//            super.writeToParcel(out, flags)
//            out.writeInt(state)
//        }
//
//        companion object {
//            val CREATOR: Creator<SavedState> = object : ClassLoaderCreator<SavedState?>() {
//                fun createFromParcel(`in`: Parcel, loader: ClassLoader?): SavedState {
//                    return SavedState(`in`, loader)
//                }
//
//                fun createFromParcel(`in`: Parcel): SavedState {
//                    return SavedState(`in`, null as ClassLoader?)
//                }
//
//                fun newArray(size: Int): Array<SavedState?> {
//                    return arrayOfNulls(size)
//                }
//            }
//        }
//    }
//
//    private inner class SettleRunnable internal constructor(
//        private val view: View,
//        private val targetState: Int
//    ) : Runnable {
//        override fun run() {
//            if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
//                ViewCompat.postOnAnimation(view, this)
//            } else {
//                setStateInternal(targetState)
//            }
//        }
//    }
//
//    @Retention(RetentionPolicy.SOURCE)
//    @RestrictTo([Scope.LIBRARY_GROUP])
//    annotation class State
//    abstract class BottomSheetCallback {
//        abstract fun onStateChanged(@NonNull var1: View?, var2: Int)
//        abstract fun onSlide(@NonNull var1: View?, var2: Float)
//    }
//
//    companion object {
//        const val STATE_DRAGGING = 1
//        const val STATE_SETTLING = 2
//        const val STATE_EXPANDED = 3
//        const val STATE_COLLAPSED = 4
//        const val STATE_HIDDEN = 5
//        const val STATE_HALF_EXPANDED = 6
//        const val PEEK_HEIGHT_AUTO = -1
//        private const val HIDE_THRESHOLD = 0.5f
//        private const val HIDE_FRICTION = 0.1f
//        fun <V : View?> from(view: V): BottomSheetBehavior<V> {
//            val params: ViewGroup.LayoutParams = view!!.layoutParams
//            return if (params !is android.support.design.widget.CoordinatorLayout.LayoutParams) {
//                throw IllegalArgumentException("The view is not a child of CoordinatorLayout")
//            } else {
//                val behavior: Behavior =
//                    (params as android.support.design.widget.CoordinatorLayout.LayoutParams).getBehavior()
//                if (behavior !is BottomSheetBehavior<*>) {
//                    throw IllegalArgumentException("The view is not associated with BottomSheetBehavior")
//                } else {
//                    behavior
//                }
//            }
//        }
//    }
// }
