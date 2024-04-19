package dev.loudbook.pastebook

import com.google.gson.JsonParser
import dev.loudbook.pastebook.mongo.Paste
import dev.loudbook.pastebook.mongo.PasteRepository
import jakarta.servlet.http.HttpServletRequest
import net.datafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.time.format.DateTimeFormatter

@RestController
class UploadController {
    @Autowired
    private lateinit var discord: Discord

    @Autowired
    lateinit var pasteRepository: PasteRepository

    val faker = Faker()

    @PostMapping("/upload")
    fun upload(request: HttpServletRequest, @RequestBody body: String): String? {
        var fileID = "${faker.cat().name().lowercase()}-${faker.dog().name().lowercase()}-${faker.horse().name().lowercase()}-${faker.food().ingredient().lowercase()}"
        fileID = fileID.replace(" ", "")

        val start = Instant.now()
        val sinceTheEpoch = start.toEpochMilli()

        val title = request.getHeader("title") ?: return null
        val reportBook = request.getHeader("reportBook")?.toBoolean() ?: false
        val onlyPastebook = request.getHeader("onlyPastebook")?.toBoolean() ?: false

        val filteredBody = if (onlyPastebook) {
            ContentScanner.scanContent(body)
        } else {
            body
        }

        val paste = Paste(fileID, title, filteredBody, sinceTheEpoch, null, reportBook)
        val pastebookURL = uploadPastebook(paste) ?: return null

        if (onlyPastebook) {
            val discordID = try {
                discord.send(title, pastebookURL, null).toLong()
            } catch (e: NullPointerException) {
                0L
            }

            paste.discordID = discordID

            pasteRepository.save(paste)
            return pastebookURL
        }

        val pasteGGURL = uploadPasteGG(paste)
        val discordID = discord.send(title, pastebookURL, pasteGGURL)

        paste.discordID = discordID.toLong()

        pasteRepository.save(paste)

        if (pasteGGURL == null) {
            return pastebookURL
        }

        return "$pastebookURL or $pasteGGURL"
    }

    fun uploadPastebook(paste: Paste): String? {
        val url = "https://pastebook.dev/pastes/${paste.id}"
        return url
    }

    fun uploadPasteGG(paste: Paste): String? {
        val controlRegex = Regex("[\\x00-\\x1F\\x7F]")

        var flattenedContent = paste.content.replace("\n", "\\n").replace("\"", "\\\"")
        flattenedContent = flattenedContent.replace("\r", "\\r")
        flattenedContent = flattenedContent.replace(controlRegex, "")

        val expire = System.currentTimeMillis() + 1000 * 60 * 60 * 9
        val date = Instant.ofEpochMilli(expire).toString()
        val iso = date.format(DateTimeFormatter.ISO_INSTANT)

        try {
            val json = """
            {
                "name": "${paste.title}",
                "description": "New paste created: ${paste.title}",
                "expires": "$iso",
                "files": [
                    {
                        "name": "${paste.id}",
                        "content": {
                            "format": "text",
                            "value": "$flattenedContent"
                        }
                    }
                ]
            }
        """.trimIndent()

            val client = HttpClient.newBuilder().build()
            val outgoingRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.paste.gg/v1/pastes"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build()

            val response = client.send(outgoingRequest, HttpResponse.BodyHandlers.ofString())
            val responseBody = response.body()
            println(responseBody)
            val responseJson = JsonParser.parseString(responseBody).asJsonObject
            val id = responseJson.get("result").asJsonObject.get("id").asString
            return "https://paste.gg/p/anonymous/$id"
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}