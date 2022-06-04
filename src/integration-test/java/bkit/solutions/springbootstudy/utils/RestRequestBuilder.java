package bkit.solutions.springbootstudy.utils;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestRequestBuilder {

  public static MockHttpServletRequestBuilder postJson(String path) {
    return post(path)
        .contentType(MediaType.APPLICATION_JSON_VALUE);
  }
}
