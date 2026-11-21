package me.rerere.rikkahub.utils

import android.content.Intent
import android.net.Uri
import androidx.core.content.IntentCompat

/** Read Android share attachments without accepting file paths into this app's private storage. */
internal fun Intent.sharedImageUris(packageName: String): List<Uri> {
    if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return emptyList()
    val streams = runCatching {
        if (action == Intent.ACTION_SEND_MULTIPLE) {
            IntentCompat.getParcelableArrayListExtra(this, Intent.EXTRA_STREAM, Uri::class.java).orEmpty()
        } else {
            listOfNotNull(IntentCompat.getParcelableExtra(this, Intent.EXTRA_STREAM, Uri::class.java))
        }
    }.getOrDefault(emptyList())
    val attachments = streams.ifEmpty {
        val clip = clipData
        if (clip == null) emptyList() else (0 until clip.itemCount).mapNotNull { clip.getItemAt(it).uri }
    }
    return attachments.filter { uri ->
        uri.scheme == "content" &&
            (uri.authority != "$packageName.fileprovider" || uri.path == "/cache/shortcut_camera_image.jpg") &&
            uri.authority != "$packageName.documents"
    }.distinct()
}
