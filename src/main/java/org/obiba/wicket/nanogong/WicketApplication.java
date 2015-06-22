package org.obiba.wicket.nanogong;

import org.apache.wicket.protocol.http.WebApplication;
import org.obiba.wicket.nanogong.HomePage;

public class WicketApplication extends WebApplication {
  public WicketApplication() {
  }

  public Class<HomePage> getHomePage() {
    return HomePage.class;
  }
}
