-- Script de inicialización de base de datos para MSLA Challenge

-- Crear schema exchange si no existe
CREATE SCHEMA IF NOT EXISTS exchange;

-- Dar permisos al usuario
GRANT ALL PRIVILEGES ON SCHEMA exchange TO msla_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA exchange TO msla_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA exchange TO msla_user;

-- Crear extensiones útiles
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Opcional: Crear función para timestamps automáticos
CREATE OR REPLACE FUNCTION exchange.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Comentario informativo
COMMENT ON SCHEMA exchange IS 'Schema para el sistema de intercambio de monedas MSLA Challenge'; 