//package com.alcw.controller;
//
//import com.alcw.dto.LawRequest;
//import com.alcw.dto.LawResponse;
//import com.alcw.service.LawService;
//import jakarta.validation.Valid;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/law")
//public class LawController {
//    private final LawService lawService;
//
//    public LawController(LawService lawService) {
//        this.lawService = lawService;
//    }
//
//    @PostMapping("/consult")
//    public ResponseEntity<LawResponse> consultLaw(@Valid @RequestBody LawRequest request) {
//        String question = request.getQuestion();
//
//        if (!lawService.isQuestionValid(question)) {
//            return ResponseEntity.ok(
//                    new LawResponse("Please ask questions related to art and artists' law only. "
//                            + "Topics include copyright, intellectual property, and legal issues for artists.")
//            );
//        }
//
//        String aiResponse = lawService.getAIResponse(question);
//        return ResponseEntity.ok(new LawResponse(aiResponse));
//    }
//}
