package com.backbase.sample.segmentation.recommendation.upload;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/upload")
public class RecommendationReportUploadController {

    private final RecommendationReportStorageService recommendationReportStorageService;

    public RecommendationReportUploadController(RecommendationReportStorageService recommendationReportStorageService) {
        this.recommendationReportStorageService = recommendationReportStorageService;
    }

    @GetMapping
    public String uploadPage(Model model) {
        return "upload";
    }

    @PostMapping
    public String handleReportFileUpload(@RequestParam(name = "file") MultipartFile file, RedirectAttributes redirectAttributes) {
        recommendationReportStorageService.store(file);
        redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename() + "!");
        return "redirect:/upload";
    }

}
