package org.obiba.wicket.nanogong;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.obiba.wicket.nanogong.NanoGongApplet;
import org.obiba.wicket.nanogong.NanoGongApplet.Format;
import org.obiba.wicket.nanogong.NanoGongApplet.Option;
import org.obiba.wicket.nanogong.NanoGongApplet.Rate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HomePage extends WebPage {
  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public HomePage(PageParameters parameters) {
    this.add(new Component[]{new Label("message", "If you see this message wicket is properly configured and running")});
    final HashMap options = new HashMap();
    options.put(Option.AudioFormat, Format.PCM);
    options.put(Option.SamplingRate, Rate._16000);
    options.put(Option.MaxDuration, "3");
    options.put(Option.ShowSpeedButton, "false");
    options.put(Option.ShowSaveButton, "false");
    options.put(Option.ShowTime, "true");
    options.put(Option.Color, "#FFFFFF");
    options.put(Option.AudioHandlerListener, "potatoe");
    this.add(new Component[]{new NanoGongApplet("applet", "140", "60", options) {
      private static final long serialVersionUID = 1L;

      public void onAudioData(FileUpload fileUpload) {
        HomePage.this.logger.info("fileUpload: {}", fileUpload.getClientFileName());

        try {
          fileUpload.writeTo(new File("/tmp/audio"));
        } catch (IOException var3) {
          throw new RuntimeException(var3);
        }
      }

      protected void onAudioDataProcessed(AjaxRequestTarget target) {
        HomePage.this.logger.info("target: {}", target);
      }
    }});
  }
}
