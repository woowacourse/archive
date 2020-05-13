package camp.nextstep.slack

import camp.nextstep.http.Rest
import camp.nextstep.slack.Mapper.toHistory
import camp.nextstep.slack.Mapper.toUser
import ch.qos.logback.core.CoreConstants.EMPTY_STRING
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.sql.Timestamp
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

const val HOST: String = "https://slack.com/api/"
const val API_HISTORY = "conversations.history"
const val API_REPLY = "conversations.replies"
const val API_USERS = "users.list"

@Service
class SlackRepository {

    @Autowired
    lateinit var slackRest: SlackRest

    fun retrieve(token: String, channel: String): Conversations {
        val history = toHistory(request(UrlFormatter.make(API_HISTORY, token, channel)))
        return retrieveAnswers(history, token, channel)
    }

    fun retrieveAnswers(history: History, token: String, channel: String): Conversations {
        val conversations = Conversations()
        history.messages.forEach {
            conversations.add(it, toHistory(request(UrlFormatter.make(API_REPLY, token, channel, it.ts))))
        }
        return conversations
    }

    fun retrieveUsers(token: String): User {
        return toUser(request(UrlFormatter.make(API_USERS, token)))
    }

    fun request(url: String): String? {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        return slackRest.request(HttpMethod.GET, url, null, headers).body
    }
}

object DateTimeConverter {
    private const val UNNECESSARY_CHAR: String = "."
    private const val SLACK_TIMESTAMP_LENGTH = 13

    fun toLocalDateTime(timestamp: String): LocalDateTime = Timestamp(convert(timestamp)).toLocalDateTime()

    private fun convert(timestamp: String): Long =
            timestamp
                    .replace(UNNECESSARY_CHAR, "")
                    .substring(0, SLACK_TIMESTAMP_LENGTH).toLong()
}

class Url(
        private val api: String,
        private val token: String,
        private val channel: String,
        private val ts: String
) {
    fun get() = "${HOST}${api}?token=${token}${getChannel()}${getTs()}"

    private fun getChannel(): String = if (channel.isNullOrBlank()) EMPTY_STRING else "&channel=$channel"
    private fun getTs(): String = if (ts.isNullOrBlank()) EMPTY_STRING else "&ts=$ts"
}

object UrlFormatter {
    fun make(api: String, token: String, channel: String = EMPTY_STRING, ts: String = EMPTY_STRING): String {
        return Url(api, token, channel, ts).get()
    }
}

object Mapper {
    private val mapper: ObjectMapper = jacksonObjectMapper()

    fun toHistory(body: String?): History = mapper.readValue(body, History::class.java)
    fun toUser(body: String?): User = mapper.readValue(body, User::class.java)
}

@Component
class SlackRest : Rest<MultiValueMap<String, String>> {
    override fun request(method: HttpMethod,
                         url: String,
                         contents: MultiValueMap<String, String>?,
                         headers: HttpHeaders): ResponseEntity<String> {
        logger.debug("request url : {}, params : {}", url, contents)
        return RestTemplate().exchange(
                url,
                method,
                HttpEntity(contents, headers),
                String::class.java
        )
    }
}