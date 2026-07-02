package org.vaadin.examples.form.ui.components;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.vaadin.examples.form.data.AvatarImage;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.server.StreamResource;

/**
 * A custom Vaadin component that allows users to upload an avatar image.
 * <p>
 * Can be used with the {@link Binder}. Note the type below; this Component can
 * only modify {@link AvatarImage} data.
 */
public class AvatarField extends CustomField<AvatarImage> {

    /**
     * We store the value here.
     */
    private AvatarImage value;

    /**
     * This is where any upload content will be written to
     */
    private ByteArrayOutputStream outputStream;

    private Image currentAvatar;
    private Upload upload;

    public AvatarField(String caption) {
        this();
        setLabel(caption);
    }

    public AvatarField() {

        // <img> that shows the current avatar
        currentAvatar = new Image();
        currentAvatar.setAlt("avatar image");
        currentAvatar.setMaxHeight("100px");
        currentAvatar.getStyle().set("margin-right", "15px");
        currentAvatar.setVisible(false); // see updateImage()

        // create the upload component and delegate actions to the receiveUpload method
        upload = new Upload(this::receiveUpload);
        upload.getStyle().set("flex-grow", "1");

        // listen to state changes
        upload.addSucceededListener(e -> uploadSuccess(e));

        upload.addFailedListener(e -> setFailed(e.getReason().getMessage()));
        upload.addFileRejectedListener(e -> setFailed(e.getErrorMessage()));

        // only allow images to be uploaded
        upload.setAcceptedFileTypes("image/*");

        // only allow single file at a time
        upload.setMaxFiles(1);

        // set max file size to 1 MB
        upload.setMaxFileSize(1 * 1024 * 1024);

        // component layouting
        Div wrapper = new Div();
        wrapper.add(currentAvatar, upload);
        wrapper.getStyle().set("display", "flex");
        wrapper.getStyle().set("border", "3px solid");
        wrapper.getStyle().set("border-image", "linear-gradient(135deg, #FF6B35 0%, #FFA500 50%, #FFD700 100%)");
        wrapper.getStyle().set("border-radius", "20px");
        wrapper.getStyle().set("background", "linear-gradient(135deg, rgba(255, 107, 53, 0.12) 0%, rgba(255, 165, 0, 0.12) 50%, rgba(255, 215, 0, 0.08) 100%)");
        wrapper.getStyle().set("padding", "3px");
        wrapper.getStyle().set("box-shadow", "0 12px 48px rgba(255, 107, 53, 0.3), 0 0 24px rgba(255, 165, 0, 0.25), 0 0 2px rgba(255, 215, 0, 0.5) inset");
        wrapper.getStyle().set("backdrop-filter", "blur(6px)");
        wrapper.getStyle().set("margin-bottom", "18px");
        wrapper.getStyle().set("transition", "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)");
        add(wrapper);
    }

    /*
     * We need to implement this method so that this class works with the Binder.
     * This method should return the current value.
     */
    @Override
    protected AvatarImage generateModelValue() {
        return value;
    }

    /*
     * We need to implement this method so that this class works with the Binder.
     * This method should store the given value and update the visuals to the new
     * value.
     */
    @Override
    protected void setPresentationValue(AvatarImage newPresentationValue) {
        value = newPresentationValue;
        updateImage();
    }

    /**
     * Called when a user initializes an upload.
     * <p>
     * We prepare the bean and a destination for the binary data; Vaadin will take
     * care of the actual network operations.
     */
    private OutputStream receiveUpload(String fileName, String mimeType) {

        // clear old errors for better user experience
        setInvalid(false);

        // create new value bean to store the data
        value = new AvatarImage();
        value.setName(fileName);
        value.setMime(mimeType);

        // set up receiving Stream
        outputStream = new ByteArrayOutputStream();
        return outputStream;
    }

    /**
     * Called when an upload is successfully completed.
     */
    private void uploadSuccess(SucceededEvent e) {

        // store the binary data into our bean
        value.setImage(outputStream.toByteArray());

        // fire value changes so that Binder can do its thing
        setModelValue(value, true);

        // show the new image
        updateImage();

        // clear the upload component 'finished files' list for a cleaner appearance.
        // there is yet no API for it on the server side, see
        // https://github.com/vaadin/vaadin-upload-flow/issues/96
        upload.getElement().executeJs("this.files=[]");
    }

    /**
     * Shows an error message to the user.
     */
    private void setFailed(String message) {
        setInvalid(true);
        setErrorMessage(message);
    }

    /**
     * Updates avatar image content or hide if empty
     */
    private void updateImage() {
        if (value != null && value.getImage() != null) {
            currentAvatar.setSrc(new StreamResource("avatar", () -> new ByteArrayInputStream(value.getImage())));
            currentAvatar.setVisible(true);
        } else {
            currentAvatar.setSrc("");
            currentAvatar.setVisible(false);
        }
    }
}
