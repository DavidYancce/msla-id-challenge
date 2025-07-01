package msla.challenge.mslachallenge.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import msla.challenge.mslachallenge.entity.User;
import msla.challenge.mslachallenge.service.UserService;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final UserService userService;

    @Autowired
    public DataLoader(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(String... args) throws Exception {
        loadInitialData();
    }

    private void loadInitialData() {
        logger.info("Cargando datos iniciales...");

        if (!userService.existsByUsername("admin")) {
            User adminUser = userService.createUser(
                    "admin",
                    "password",
                    "admin@mslachallenge.com",
                    "Administrator",
                    "MSLA");
            logger.info("Usuario admin creado con ID: {}", adminUser.getId());
        } else {
            logger.info("Usuario admin ya existe");
        }

        if (!userService.existsByUsername("testuser")) {
            User testUser = userService.createUser(
                    "testuser",
                    "testpass",
                    "test@mslachallenge.com",
                    "Test",
                    "User");
            logger.info("Usuario de prueba creado con ID: {}", testUser.getId());
        } else {
            logger.info("Usuario de prueba ya existe");
        }

        logger.info("Datos iniciales cargados exitosamente");
    }
}