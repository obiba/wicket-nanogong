package org.obiba.wicket.nanogong;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.link.ILinkListener;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.IMultipartWebRequest;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.template.PackagedTextTemplate;
import org.apache.wicket.util.upload.FileItem;
import org.apache.wicket.util.upload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NanoGongApplet extends FormComponent<FileUpload> implements ILinkListener, IHeaderContributor {
  private static final long serialVersionUID = 1L;
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final ResourceReference appletReference = new ResourceReference(NanoGongApplet.class, "nanogong.jar");
  private Map<NanoGongApplet.Option, Object> options;
  private static int listenerId = 0;
  private final String listenerObject;
  private transient FileUpload fileUpload;
  private AbstractDefaultAjaxBehavior ajaxBehavior;

  public NanoGongApplet(String id, String width, String height, Map<NanoGongApplet.Option, Object> options) {
    super(id, new Model());
    this.listenerObject = "nanoGong" + listenerId++;
    this.options = options;
    if(this.options == null) {
      this.options = new HashMap();
    }

    this.options.put(NanoGongApplet.Option.AudioHandlerListener, this.listenerObject);
    this.setMarkupId("applet_" + this.listenerObject);
    this.setOutputMarkupId(true);
    this.add(new IBehavior[]{new AttributeModifier("type", true, new Model("application/x-java-applet"))});
    if(!Strings.isEmpty(width)) {
      this.add(new IBehavior[]{new AttributeModifier("width", true, new Model(width))});
    }

    if(!Strings.isEmpty(height)) {
      this.add(new IBehavior[]{new AttributeModifier("height", true, new Model(height))});
    }

    this.ajaxBehavior = new AbstractDefaultAjaxBehavior() {
      private static final long serialVersionUID = 1L;

      protected void respond(AjaxRequestTarget target) {
        NanoGongApplet.this.onAudioDataProcessed(target);
      }
    };
    this.add(new IBehavior[]{this.ajaxBehavior});
  }

  public void onComponentTagBody(MarkupStream markupStream, ComponentTag componentTag) {
    this.checkComponentTag(componentTag, "object");
    StringBuilder paramBuilder = new StringBuilder();
    paramBuilder.append("<param name=\"code\" value=\"gong.NanoGong\" />\n");
    paramBuilder.append("<param name=\"archive\" value=\"" + this.urlFor(this.appletReference) + "\" />\n");
    if(this.options != null) {
      Iterator i$ = this.options.entrySet().iterator();

      while(i$.hasNext()) {
        Entry entry = (Entry)i$.next();
        NanoGongApplet.Option option = (NanoGongApplet.Option)entry.getKey();
        Object value = entry.getValue();
        if(value != null) {
          if(!value.getClass().isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Option " + option + " expects a value of type " + option.type + ". Found " + value.getClass());
          }

          paramBuilder.append("<param name=\"" + option + "\" value=\"" + value + "\" />\n");
        }
      }
    }

    this.replaceComponentTagBody(markupStream, componentTag, paramBuilder.toString());
  }

  public void renderHead(IHeaderResponse response) {
    HashMap variables = new HashMap();
    variables.put("nanoGong", this.listenerObject);
    variables.put("nanoGongAppletId", this.getMarkupId());
    variables.put("postUrl", this.postUrl());
    variables.put("wicketAjaxUrl", this.ajaxBehavior.getCallbackUrl());
    PackagedTextTemplate template = new PackagedTextTemplate(NanoGongApplet.class, "NanoGongApplet.js");
    template.interpolate(variables);
    response.renderOnDomReadyJavascript(template.getString());
  }

  public NanoGongApplet getApplet() {
    return (NanoGongApplet)this.get(0);
  }

  public String postUrl() {
    return this.urlFor(ILinkListener.INTERFACE).toString();
  }

  public void onLinkClicked() {
    this.logger.debug("onLinkClicked()");

    try {
      WebRequest wre = ((WebRequest)this.getRequest()).newMultipartWebRequest(this.getMaxSize());
      this.getRequestCycle().setRequest(wre);
      Map e1 = ((IMultipartWebRequest)wre).getFiles();
      if(e1.size() > 0) {
        this.fileUpload = new FileUpload((FileItem)e1.values().iterator().next());
        this.onAudioData(this.fileUpload);
        this.getRequestCycle().setRequestTarget(new StringRequestTarget("true"));
      }
    } catch (WicketRuntimeException var4) {
      this.logger.error("WicketRuntimeException", var4);
      if(var4.getCause() == null || !(var4.getCause() instanceof FileUploadException)) {
        throw var4;
      }

      FileUploadException e = (FileUploadException)var4.getCause();
      HashMap model = new HashMap();
      model.put("exception", e);
      model.put("maxSize", this.getMaxSize());
      this.onFileUploadException((FileUploadException)var4.getCause(), model);
      this.getRequestCycle().setRequestTarget(new StringRequestTarget("false"));
    }

  }

  protected void onAudioData(FileUpload fileUpload) {
  }

  protected void onAudioDataProcessed(AjaxRequestTarget target) {
  }

  protected void onFileUploadException(FileUploadException cause, Map<String, Object> model) {
  }

  protected Bytes getMaxSize() {
    return this.getApplication().getApplicationSettings().getDefaultMaximumUploadSize();
  }

  public static enum Rate {
    _8000,
    _11025,
    _16000,
    _22050,
    _32000,
    _44100;

    private Rate() {
    }

    public String toString() {
      String s = super.toString();
      return s.substring(1);
    }

    public static NanoGongApplet.Rate parse(String s) {
      NanoGongApplet.Rate[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
        NanoGongApplet.Rate r = arr$[i$];
        if(r.toString().equals(s)) {
          return r;
        }
      }

      return null;
    }
  }

  public static enum Format {
    PCM,
    ImaADPCM,
    SPEEX;

    private Format() {
    }
  }

  public static enum Option {
    ShowRecordButton,
    ShowSaveButton,
    ShowSpeedButton,
    ShowAudioLevel,
    ShowTime,
    Color,
    SoundFileURL,
    StartTime,
    EndTime,
    AudioFormat(NanoGongApplet.Format.class),
    SamplingRate(NanoGongApplet.Rate.class),
    MaxDuration,
    AudioHandlerListener,
    PostUrl;

    private final Class<?> type;

    private Option() {
      this(String.class);
    }

    private Option(Class<?> type) {
      this.type = type;
    }
  }
}
