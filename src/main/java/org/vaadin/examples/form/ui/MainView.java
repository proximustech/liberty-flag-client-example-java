package org.vaadin.examples.form.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.examples.form.data.UserDetails;
import org.vaadin.examples.form.data.UserDetailsService;
import org.vaadin.examples.form.data.UserDetailsService.ServiceException;
import org.vaadin.examples.form.ui.components.AvatarField;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.Route;

import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.component.UI;

import com.vaadin.flow.theme.lumo.Lumo;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import net.proximustech.libertyflag.FlagTool;

/**
 * This is the default (and only) view in this example.
 * <p>
 * It demonstrates how to create a form using Vaadin and the Binder. The backend
 * service and data class are in the <code>.data</code> package.
 */
@Route("")
public class MainView extends VerticalLayout implements HasUrlParameter<String> {

    private Checkbox allowMarketingBox;
    private PasswordField passwordField1;
    private PasswordField passwordField2;

    private UserDetailsService service;
    private BeanValidationBinder<UserDetails> binder;
    private Map<String, List<String>> parametersMap;

    @Override
    public void setParameter(BeforeEvent event,@OptionalParameter String parameter) {

        Location location = event.getLocation();
        QueryParameters queryParameters = location.getQueryParameters();

        this.parametersMap = queryParameters.getParameters();
        this.MainView(new UserDetailsService());
    }


    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    private void MainView(UserDetailsService service) {

        String plan="premium";
        String age="30";
        String user="2222";

        try {

            plan = this.parametersMap.get("plan").get(0).toString();
            age = this.parametersMap.get("age").get(0).toString();
            user = this.parametersMap.get("user").get(0).toString();

        } catch (Exception e) {}   

        this.service = service;

        /*
         * Create the components we'll need
         */

        //H3 title = new H3("DEMO Signup form");
        H3 title = new H3(String.format("(Plan %s, Age %s) Signup Form", plan,age));

        TextField firstnameField = new TextField("First name");
        TextField lastnameField = new TextField("Last name");
        TextField handleField = new TextField("User handle");

        // This is a custom field we create to handle the field 'avatar' in our data. It
        // work just as any other field, e.g. the TextFields above. Instead of a String
        // value, it has an AvatarImage value.
        AvatarField avatarField = new AvatarField("Select Avatar image");

        // We'll need these fields later on so let's store them as class variables
        allowMarketingBox = new Checkbox("Allow Marketing?");
        allowMarketingBox.getStyle().set("padding-top", "10px");
        EmailField emailField = new EmailField("Email");
        emailField.setVisible(false);

        passwordField1 = new PasswordField("Wanted password");
        passwordField2 = new PasswordField("Password again");

        Span errorMessage = new Span();

        Button submitButton = new Button("SUBMIT FORM");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);


        H3 libertyFlagSeparator = new H3("Liberty Flag Demo Buttons:");

        Integer dashesNumber = FlagTool.client.getIntegerFlagValue("app.dashes-number",1);
        
        String dashes = "";
        for(Integer dashesIndex=0;dashesIndex < dashesNumber;dashesIndex++){
            dashes = dashes + "-";
        }
        H3 htmlLine = new H3(dashes);

        Button aProfileButton = new Button("PREMIUM PROFILE", e -> {UI.getCurrent().getPage().open("/?plan=premium&age=30&user=2222", "_self");});
        aProfileButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button bProfileButton = new Button("STANDARD PROFILE", e -> {UI.getCurrent().getPage().open("/?plan=standard&age=25&user=3333", "_self");});
        bProfileButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        Button testProfileButton = new Button("QA TEST USER", e -> {UI.getCurrent().getPage().open("/?plan=standard&age=35&user=1111", "_self");});
        testProfileButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);        

        /*
         * Build the visible layout
         */

        // Create a FormLayout with all our components. The FormLayout doesn't have any
        // logic (validation, etc.), but it allows us to configure Responsiveness from
        // Java code and its defaults looks nicer than just using a VerticalLayout.
        FormLayout formLayout = new FormLayout();

        formLayout.add(title);
        formLayout.add(firstnameField);
        formLayout.add(lastnameField);
        formLayout.add(handleField);        
        formLayout.add(passwordField1);
        formLayout.add(passwordField2);
        formLayout.add(allowMarketingBox);
        formLayout.add(emailField);

        switch(FlagTool.client.getStringFlagValue("app.theme","Light")) {
          case "Light":
            // code block
            break;
          case "Dark":
            this.getElement().setAttribute("theme", Lumo.DARK);
            UI.getCurrent().getPage().executeJs("document.documentElement.setAttribute('theme', 'dark');");
            title.getStyle().set("margin-top", "15px");
            break;
          case "Wall":
            this.getStyle().set("background-color", "floralwhite");
            break;
          default:
            // code block
        }


        /*
        if (FlagTool.client.booleanFlagIsTrue("images.show-avatar-field",0)) {
            formLayout.add(avatarField);
        }
        */     

        HashMap<String, String> avatarFlagData = new HashMap<String, String>();

        avatarFlagData.put("plan",plan);
        avatarFlagData.put("age",age);
        avatarFlagData.put("user",user);

        if (FlagTool.client.booleanFlagIsTrue("images.show-avatar-field",0,avatarFlagData)) {
            formLayout.add(avatarField);
        }        
        
        formLayout.add(errorMessage);
        formLayout.add(submitButton);

        formLayout.add(htmlLine);
        formLayout.add(libertyFlagSeparator);

        formLayout.add(aProfileButton);        
        formLayout.add(bProfileButton);
        formLayout.add(testProfileButton);

        // Restrict maximum width and center on page
        formLayout.setMaxWidth("500px");
        formLayout.getStyle().set("margin", "0 auto");

        // Allow the form layout to be responsive. On device widths 0-490px we have one
        // column, then we have two. Field labels are always on top of the fields.
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
                new FormLayout.ResponsiveStep("490px", 2, FormLayout.ResponsiveStep.LabelsPosition.TOP));

        // These components take full width regardless if we use one column or two (it
        // just looks better that way)
        formLayout.setColspan(title, 2);
        formLayout.setColspan(avatarField, 2);
        formLayout.setColspan(errorMessage, 2);
        formLayout.setColspan(submitButton, 2);
        formLayout.setColspan(libertyFlagSeparator, 2);
        formLayout.setColspan(htmlLine, 2);
        formLayout.setColspan(testProfileButton, 2);

        // Add some styles to the error message to make it pop out
        errorMessage.getStyle().set("color", "var(--lumo-error-text-color)");
        errorMessage.getStyle().set("padding", "15px 0");

        // Add the form to the page        
        add(formLayout);

        /*
         * Set up form functionality
         */

        /*
         * Binder is a form utility class provided by Vaadin. Here, we use a specialized
         * version to gain access to automatic Bean Validation (JSR-303). We provide our
         * data class so that the Binder can read the validation definitions on that
         * class and create appropriate validators. The BeanValidationBinder can
         * automatically validate all JSR-303 definitions, meaning we can concentrate on
         * custom things such as the passwords in this class.
         */
        binder = new BeanValidationBinder<UserDetails>(UserDetails.class);

        // Basic name fields that are required to fill in
        binder.forField(firstnameField).asRequired().bind("firstname");
        binder.forField(lastnameField).asRequired().bind("lastname");

        // The handle has a custom validator, in addition to being required. Some values
        // are not allowed, such as 'admin'; this is checked in the validator.
        binder.forField(handleField).withValidator(this::validateHandle).asRequired().bind("handle");

        // Here we use our custom Vaadin component to handle the image portion of our
        // data, since Vaadin can't do that for us. Because the AvatarField is of type
        // HasValue<AvatarImage>, the Binder can bind it automatically. The avatar is
        // not required and doesn't have a validator, but could.
        binder.forField(avatarField).bind("avatar");

        // Allow marketing is a simple checkbox
        binder.forField(allowMarketingBox).bind("allowsMarketing");
        // EmailField uses a Validator that extends one of the built-in ones.
        // Note that we use 'asRequired(Validator)' instead of
        // 'withValidator(Validator)'; this method allows 'asRequired' to
        // be conditional instead of always on. We don't want to require the email if
        // the user declines marketing messages.
        binder.forField(emailField).asRequired(new VisibilityEmailValidator("Value is not a valid email address")).bind("email");

        // Only ask for email address if the user wants marketing emails
        allowMarketingBox.addValueChangeListener(e -> {

            // show or hide depending on the checkbox
            emailField.setVisible(allowMarketingBox.getValue());

            // Additionally, remove the input if the user decides not to allow emails. This
            // way any input that ends up hidden on the page won't end up in the bean when
            // saved.
            if (!allowMarketingBox.getValue()) {
                emailField.setValue("");
            }
        });

        // Another custom validator, this time for passwords
        binder.forField(passwordField1).asRequired().withValidator(this::passwordValidator).bind("password");
        // We won't bind passwordField2 to the Binder, because it will have the same
        // value as the first field when correctly filled in. We just use it for
        // validation.

        // The second field is not connected to the Binder, but we want the binder to
        // re-check the password validator when the field value changes. The easiest way
        // is just to do that manually.
        passwordField2.addValueChangeListener(e -> {

            // The user has modified the second field, now we can validate and show errors.
            // See passwordValidator() for how this flag is used.
            enablePasswordValidation = true;

            binder.validate();
        });

        // A label where bean-level error messages go
        binder.setStatusLabel(errorMessage);

        // And finally the submit button
        submitButton.addClickListener(e -> {
            try {

                // Create empty bean to store the details into
                UserDetails detailsBean = new UserDetails();

                // Run validators and write the values to the bean
                binder.writeBean(detailsBean);

                // Call backend to store the data
                service.store(detailsBean);

                // Show success message if everything went well
                showSuccess(detailsBean);

            } catch (ValidationException e1) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            } catch (ServiceException e2) {

                // For some reason, the save failed in the back end.

                // First, make sure we store the error in the server logs (preferably using a
                // logging framework)
                e2.printStackTrace();

                // Notify, and let the user try again.
                errorMessage.setText("Saving the data failed, please try again");
            }
        });


    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(UserDetails detailsBean) {
        Notification notification = Notification.show("Data saved, welcome " + detailsBean.getHandle());
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        // Here you'd typically redirect the user to another view
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {

        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = passwordField2.getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    /**
     * Method that demonstrates using an external validator. Here we ask the backend
     * if this handle is already in use.
     */
    private ValidationResult validateHandle(String handle, ValueContext ctx) {

        String errorMsg = service.validateHandle(handle);

        if (errorMsg == null) {
            return ValidationResult.ok();
        }

        return ValidationResult.error(errorMsg);
    }

    /**
     * Custom validator class that extends the built-in email validator.
     * <p>
     * Ths validator checks if the field is visible before performing the
     * validation. This way, the validation is only performed when the user has told
     * us they want marketing emails.
     */
    public class VisibilityEmailValidator extends EmailValidator {

        public VisibilityEmailValidator(String errorMessage) {
            super(errorMessage);
        }

        @Override
        public ValidationResult apply(String value, ValueContext context) {

            if (!allowMarketingBox.getValue()) {
                // Component not visible, no validation
                return ValidationResult.ok();
            } else {
                // normal email validation
                return super.apply(value, context);
            }
        }
    }
}
