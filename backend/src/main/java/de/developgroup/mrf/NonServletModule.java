package de.developgroup.mrf;

import com.google.inject.AbstractModule;
import de.developgroup.mrf.controllers.CameraController;
import de.developgroup.mrf.controllers.CameraControllerImpl;
import de.developgroup.mrf.controllers.ExampleController;
import de.developgroup.mrf.controllers.ExampleControllerImpl;
import de.developgroup.mrf.servlets.example.CameraSocket;
import de.developgroup.mrf.servlets.example.ExampleSocket;

// TODO rename to NonServletModule
public class NonServletModule extends AbstractModule {
    @Override
    protected void configure(){
        // here you can list all bindings of non-servlet classes needed in the backend
        // e.g. bind(IMotorController.class).to(SimpleMotorController.class);
        bind(ExampleController.class).to(ExampleControllerImpl.class);
        bind(CameraController.class).to(CameraControllerImpl.class);

        requestStaticInjection(ExampleSocket.class);
        requestStaticInjection(CameraSocket.class);
    }
}
