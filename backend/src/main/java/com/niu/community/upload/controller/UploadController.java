package com.niu.community.upload.controller;

import com.niu.community.common.config.AppProperties;
import com.niu.community.common.model.Result;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final AppProperties appProperties;

    public UploadController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestAttribute(value = "userId", required = false) Long userId,
                                       @RequestParam("file") MultipartFile file) throws IOException {
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        String original = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "avatar.png";
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
        String fileName = UUID.randomUUID() + ext;
        Path uploadDir = Paths.get(appProperties.getUploadDir());
        Files.createDirectories(uploadDir);
        Files.copy(file.getInputStream(), uploadDir.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return Result.success("上传成功", uploadDir.resolve(fileName).toString());
    }
}
