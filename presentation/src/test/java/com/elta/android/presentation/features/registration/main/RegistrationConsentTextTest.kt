package com.elta.android.presentation.features.registration.main

import androidx.compose.ui.text.TextLinkStyles
import com.elta.android.presentation.features.registration.main.model.RegistrationAction
import com.elta.android.presentation.features.registration.main.ui.registrationConsentText
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationConsentTextTest {

    @Test
    fun `Russian resource links work across embedded line breaks`() {
        checkResourceLinks("values")
    }

    @Test
    fun `English resource links retain their own document actions`() {
        checkResourceLinks("values-en")
    }

    @Test
    fun `unmatched translation preserves the complete consent text`() {
        val text = registrationConsentText("Full consent text", "missing", "", TextLinkStyles()) {}

        assertEquals("Full consent text", text.text)
        assertEquals(0, text.getLinkAnnotations(0, text.length).size)
    }

    private fun checkResourceLinks(directory: String) {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File("src/main/res/$directory/strings.xml"))
        val elements = document.getElementsByTagName("string")
        fun resource(name: String): String {
            for (index in 0 until elements.length) {
                val node = elements.item(index)
                if (node.attributes.getNamedItem("name").nodeValue == name) {
                    return node.textContent.replace("\\n", "\n")
                }
            }
            error("Missing string: $name")
        }

        val description = resource("registration_main_description_privacy_policy")
        val policy = resource("registration_main_privacy_policy_clickable_mask")
        val personalData = resource("registration_main_personal_data_clickable_mask")
        val actions = mutableListOf<RegistrationAction>()
        val text = registrationConsentText(description, policy, personalData, TextLinkStyles(), actions::add)
        val links = text.getLinkAnnotations(0, text.length)

        assertEquals(description.replace(Regex("\\s+"), " ").trim(), text.text)
        assertEquals(2, links.size)
        assertEquals(policy, text.text.substring(links[0].start, links[0].end))
        assertEquals(personalData, text.text.substring(links[1].start, links[1].end))
        links.forEach { it.item.linkInteractionListener!!.onClick(it.item) }
        assertEquals(
            listOf(RegistrationAction.PrivacyPolicyClicked, RegistrationAction.PersonalDataClicked),
            actions
        )
    }
}
