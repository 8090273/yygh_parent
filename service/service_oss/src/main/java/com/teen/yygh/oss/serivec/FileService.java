package com.teen.yygh.oss.serivec;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author teen
 * @create 2022/4/1 9:13
 */
public interface FileService {
    String upload(MultipartFile file);
}
