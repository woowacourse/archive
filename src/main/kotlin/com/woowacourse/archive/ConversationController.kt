package com.woowacourse.archive

import com.woowacourse.dto.ConversationDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ConversationController(
    private val conversationService: ConversationService
) {
    @GetMapping("/conversations")
    fun retrieve(): ResponseEntity<List<ConversationDto>> {
        val conversationDtos = ConversationDto.listOf(conversationService.retrieve())
        return ResponseEntity.ok(conversationDtos)
    }

    @GetMapping("/archive")
    fun archiveManually(): ResponseEntity<Unit> {
        conversationService.archive()
        return ResponseEntity.ok().build()
    }
}
