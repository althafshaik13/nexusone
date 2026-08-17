package dev.nexusone.ai_copilot_service.web;

import dev.nexusone.ai_copilot_service.dto.SuggestReplyResponse;
import dev.nexusone.ai_copilot_service.dto.SummarizeResponse;
import dev.nexusone.ai_copilot_service.service.CopilotService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/copilot/tickets/{ticketId}")
public class CopilotController {

    private final CopilotService copilotService;

    public CopilotController(CopilotService copilotService) {
        this.copilotService = copilotService;
    }

    @PostMapping("/suggest-reply")
    public SuggestReplyResponse suggestReply(@PathVariable UUID ticketId, HttpServletRequest httpRequest) {
        String bearerToken = httpRequest.getHeader("Authorization");
        return new SuggestReplyResponse(copilotService.suggestReply(ticketId, bearerToken));
    }

    @PostMapping("/summarize")
    public SummarizeResponse summarize(@PathVariable UUID ticketId, HttpServletRequest httpRequest) {
        String bearerToken = httpRequest.getHeader("Authorization");
        return new SummarizeResponse(copilotService.summarize(ticketId, bearerToken));
    }
}
