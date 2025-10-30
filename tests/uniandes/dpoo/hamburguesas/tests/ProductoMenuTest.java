package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

public class ProductoMenuTest{
	private ProductoMenu pm;
	
	@BeforeEach	
	void setUp() {
		pm = new ProductoMenu("Hamburguesa Sencilla", 12000);
	}
	
	@AfterEach
	void tearDown() {
		pm = null;
	}
	
	@Test
    void constructorGettersTest() {
        assertEquals("Hamburguesa Sencilla", pm.getNombre(), "Nombre incorrecto");
        assertEquals(12000, pm.getPrecio(), "Precio base incorrecto");
    }
	@Test
    void generarTextoFacturaNombrePrecioTest() {
        String fac = pm.generarTextoFactura();
        assertTrue(fac.contains("Hamburguesa Sencilla"), "Debe incluir el nombre");
        assertTrue(fac.contains(String.valueOf(12000)), "Debe incluir el precio");
        String[] lineas = fac.split("\\R");
        assertTrue(lineas.length >= 2, "La factura debería tener al menos 2 líneas");
    }
}