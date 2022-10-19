package com.cnasoft.health.userservice.excel.service;

import com.cnasoft.health.fileapi.fegin.FileFeignClient;

import java.util.List;

/**
 * @author Administrator
 */
public interface IFileService<T> {

    String saveFile(FileFeignClient fileFeignClient, String fileName, List<T> list, Class head);
}
