package com.cnasoft.health.userservice.excel.bean;

 import lombok.Data;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.stereotype.Component;

 import javax.annotation.Resource;
 import javax.validation.Validation;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * @author Administrator
 */
@Component
@Data
public class ImportThreadPool {
    public static javax.validation.ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
}
