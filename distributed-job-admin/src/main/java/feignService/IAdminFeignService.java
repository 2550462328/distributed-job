package feignService;

import com.zhanghui.core.dto.TesseractExecutorRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.net.URI;

@FeignClient(value = "AdminFeignClient")
public interface IAdminFeignService {

    @RequestLine("POST")
    TesseractExecutorResponse sendToExecutor(URI uri, TesseractExecutorRequest request);
}
