package me.rerere.rikkahub

import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.SpannableString
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.rerere.rikkahub.utils.sharedImageUris
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SharedImagesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val first = Uri.parse("content://media/external/images/media/101")
    private val second = Uri.parse("content://media/external/images/media/102")

    @Test
    fun readsParcelableStreamAndPreservesStyledCaption() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, first)
            putExtra(Intent.EXTRA_TEXT, SpannableString("Image caption"))
        }
        assertEquals(listOf(first), intent.sharedImageUris(context.packageName))
        assertEquals("Image caption", intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString())
    }

    @Test
    fun readsMultipleImagesInOrderWithoutDuplicates() {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(first, second, first))
        }
        assertEquals(listOf(first, second), intent.sharedImageUris(context.packageName))
    }

    @Test
    fun supportsClipDataOnlySenders() {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            clipData = ClipData.newRawUri("First image", first).apply { addItem(ClipData.Item(second)) }
        }
        assertEquals(listOf(first, second), intent.sharedImageUris(context.packageName))
    }

    @Test
    fun streamExtraTakesPrecedenceOverDuplicateClipData() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, first)
            clipData = ClipData.newRawUri("First image", first)
        }
        assertEquals(listOf(first), intent.sharedImageUris(context.packageName))
    }

    @Test
    fun textSharesDoNotCreateAttachments() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "A text share")
        }
        assertTrue(intent.sharedImageUris(context.packageName).isEmpty())
    }

    @Test
    fun doesNotInterpretAStringAsAnImageUri() {
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, first.toString())
        assertTrue(intent.sharedImageUris(context.packageName).isEmpty())
    }

    @Test
    fun rejectsPrivatePathsAndSelfProviderUris() {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(
                Uri.parse("file:///data/data/${context.packageName}/files/settings.json"),
                Uri.parse("content://${context.packageName}.fileprovider/files/settings.json"),
                Uri.parse("content://${context.packageName}.documents/workspace"),
                first
            ))
        }
        assertEquals(listOf(first), intent.sharedImageUris(context.packageName))
    }

    @Test
    fun preservesTheAppsCameraShortcut() {
        val image = Uri.parse("content://${context.packageName}.fileprovider/cache/shortcut_camera_image.jpg")
        val intent = Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_STREAM, image)
        assertEquals(listOf(image), intent.sharedImageUris(context.packageName))
    }

    @Test
    fun registersSingleAndMultipleImageShares() {
        for (action in listOf(Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE)) {
            for (mime in listOf("image/png", "image/jpeg", "image/webp")) {
                val matches = context.packageManager.queryIntentActivities(
                    Intent(action).setType(mime).setPackage(context.packageName),
                    PackageManager.MATCH_DEFAULT_ONLY
                )
                assertTrue("Missing share registration: $action $mime", matches.any {
                    it.activityInfo.name == RouteActivity::class.java.name
                })
            }
        }
    }
}
