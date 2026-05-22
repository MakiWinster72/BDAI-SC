package com.gcsc.studentcenter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

  private final ClientIpResolver resolver = new ClientIpResolver();

  @Test
  void skipsLoopbackInXForwardedForChain() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "127.0.0.1, 10.8.12.34");

    assertEquals("10.8.12.34", resolver.resolve(request));
  }

  @Test
  void prefersRealIpOverLoopbackRemoteAddr() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Real-IP", "203.0.113.8");
    request.setRemoteAddr("127.0.0.1");

    assertEquals("203.0.113.8", resolver.resolve(request));
  }

  @Test
  void acceptsPrivateVpnRange() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "172.16.88.5");

    assertEquals("172.16.88.5", resolver.resolve(request));
  }

  @Test
  void loopbackOnlyFallsBackToRemoteAddr() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "127.0.0.1");
    request.setRemoteAddr("127.0.0.1");

    assertEquals("127.0.0.1", resolver.resolve(request));
  }

  @Test
  void directVpnClientWithoutProxyHeaders() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("10.66.88.12");

    assertEquals("10.66.88.12", resolver.resolve(request));
  }

  @Test
  void directMotionProStyle172VpnClient() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("172.20.88.12");

    assertEquals("172.20.88.12", resolver.resolve(request));
  }

  @Test
  void skipsLoopbackHeaderAndUses172RemoteAddr() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "127.0.0.1");
    request.setRemoteAddr("172.20.88.12");

    assertEquals("172.20.88.12", resolver.resolve(request));
  }

  @Test
  void skipsLoopbackHeaderAndUsesVpnRemoteAddr() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Forwarded-For", "127.0.0.1");
    request.setRemoteAddr("10.66.88.12");

    assertEquals("10.66.88.12", resolver.resolve(request));
  }

  @Test
  void prefersRealIpHeaderOverLoopbackRemote() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader("X-Real-IP", "10.12.34.56");
    request.setRemoteAddr("127.0.0.1");

    assertEquals("10.12.34.56", resolver.resolve(request));
  }

  @Test
  void isLoopbackDetectsIpv4AndIpv6() {
    assertTrue(ClientIpResolver.isLoopback("127.0.0.1"));
    assertTrue(ClientIpResolver.isLoopback("::ffff:127.0.0.1"));
    assertTrue(ClientIpResolver.isLoopback("::1"));
  }
}
