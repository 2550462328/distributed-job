package com.zhanghui.feignService;

import com.zhanghui.core.dto.TesseractAdminJobNotify;
import com.zhanghui.core.dto.TesseractAdminRegistryRequest;
import com.zhanghui.core.dto.TesseractExecutorResponse;
import com.zhanghui.core.dto.TesseractHeartbeatRequest;
import feign.RequestLine;
import org.springframework.cloud.openfeign.FeignClient;

import java.net.URI;

@FeignClient("ClientFeignClient")
public interface IClientFeignService {
    @RequestLine("POST")
    TesseractExecutorResponse registry(URI uri, TesseractAdminRegistryRequest request);

    @RequestLine("POST")
    TesseractExecutorResponse notify(URI uri, TesseractAdminJobNotify tesseractAdminJobNotify);

    @RequestLine("POST")
    TesseractExecutorResponse heartbeat(URI uri, TesseractHeartbeatRequest heartBeatRequest);
}
