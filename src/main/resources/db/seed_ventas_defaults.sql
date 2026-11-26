-- Añade los métodos de pago y la configuración de descuento iniciales que falten.
-- Conserva los valores que cada negocio ya haya parametrizado.
BEGIN;
INSERT INTO rm_detalles.metodos_pago (negocio_id,codigo,nombre,activo)
SELECT n.id,m.codigo,m.nombre,TRUE
FROM rm_detalles.negocios n
CROSS JOIN (VALUES
 ('EFECTIVO','Efectivo'),
 ('TRANSFERENCIA','Transferencia'),
 ('TARJETA','Tarjeta'),
 ('OTRO','Otro')
) AS m(codigo,nombre)
WHERE n.activo=TRUE
ON CONFLICT (negocio_id,codigo) DO NOTHING;

INSERT INTO rm_detalles.configuraciones_negocio (negocio_id,clave,valor,descripcion)
SELECT n.id,'ventas.descuento_maximo_porcentaje','100'::jsonb,'Porcentaje máximo de descuento permitido'
FROM rm_detalles.negocios n
WHERE n.activo=TRUE
ON CONFLICT (negocio_id,clave) DO NOTHING;
COMMIT;
