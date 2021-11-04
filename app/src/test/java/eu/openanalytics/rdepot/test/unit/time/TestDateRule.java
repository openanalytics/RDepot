/**
 * R Depot
 *
 * Copyright (C) 2012-2021 Open Analytics NV
 *
 * ===========================================================================
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Apache License as published by
 * The Apache Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Apache License for more details.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/>
 */
package eu.openanalytics.rdepot.test.unit.time;

import java.util.Date;
import org.junit.rules.ExternalResource;
import eu.openanalytics.rdepot.time.DateProvider;

public class TestDateRule extends ExternalResource {

  private Date testDate;

  public TestDateRule() {
    this(new Date());
  }

  public TestDateRule(Date testDate) {
    this.testDate = testDate;
    DateProvider.setTestDate(testDate);
  }

  public void setTestDate(Date testDate) {
    this.testDate = Date.from(testDate.toInstant());
  }

  public Date getTestDate() {
    return testDate;
  }

  @Override
  protected void after() {
    DateProvider.clearTestDate();
  }
}
