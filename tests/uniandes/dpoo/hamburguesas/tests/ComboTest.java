package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;

public class ComboTest{
	
	private Combo combo;
	
	@BeforeEach
	void setUp() {
		
        ArrayList<ProductoMenu> items = new ArrayList<>();
        items.add(new ProductoMenu("Hamburguesa", 10000));
        items.add(new ProductoMenu("Papas", 6000));
        items.add(new ProductoMenu("Gaseosa", 4000));
        
        combo = new Combo("especial", 0.07, items);

        System.out.println("creando nuevo combo...");
    }

	
	@AfterEach
	void tearDown() {
		combo = null;
		System.out.println("limpiando info...");	
	}
	
	@Test
	void testGetNombreCombo() {
		
		String nombre = combo.getNombre();
		assertEquals("especial",nombre,"no existe ese nombre de combo");	
	}
	
	@Test 
	void testGetPrecioCombo() {
		int precioEsperado = (int) (20000 * (1 - 0.07));
		assertEquals(precioEsperado, combo.getPrecio(), "el precio del combo no coincide");
	}
	
	
	@Test 
	void testGenerarTextoFacturaCombo() {
	    String factura = combo.generarTextoFactura();

	    assertTrue(factura.contains("Combo"), "Debe mencionar la palabra 'Combo'");
	    assertTrue(factura.contains("especial"), "Debe incluir el nombre del combo");
	    assertTrue(factura.contains("0.07"), "Debe incluir el descuento del combo (0.07)");
	    
	    int precioEsperado = (int)((10000 + 6000 + 4000) * (1 - 0.07));
	    assertTrue(factura.contains(String.valueOf(precioEsperado)), "Debe incluir el precio calculado correctamente");
	}

	
}
