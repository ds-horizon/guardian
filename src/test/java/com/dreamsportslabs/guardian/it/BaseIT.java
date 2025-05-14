package com.dreamsportslabs.guardian.it;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class BaseIT {

  @Test
  public void test1() {
    assertThat(false, is(false));
  }
}
