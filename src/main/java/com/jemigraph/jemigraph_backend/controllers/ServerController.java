package com.jemigraph.jemigraph_backend.controllers;

import com.jemigraph.jemigraph_backend.services.OshiService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
@RequiredArgsConstructor
public class ServerController {
  private final OshiService oshiService;

  @GetMapping("/status")
  public Map<String, Object> getServerStatus() {
    return oshiService.getSystemMetrics();
  }
}
