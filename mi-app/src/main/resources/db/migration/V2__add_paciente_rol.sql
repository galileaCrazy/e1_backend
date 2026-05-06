-- Agregar rol PACIENTE al check constraint de usuario
ALTER TABLE usuario DROP CONSTRAINT IF EXISTS usuario_rol_check;
ALTER TABLE usuario ADD CONSTRAINT usuario_rol_check
    CHECK (rol IN ('ADMIN', 'MEDICO', 'PACIENTE'));
