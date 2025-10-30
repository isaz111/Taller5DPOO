package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;

import uniandes.dpoo.hamburguesas.mundo.ProductoAjustado;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
import uniandes.dpoo.hamburguesas.mundo.Ingrediente;

public class ProductoAjustadoTest {

    private ProductoMenu base;
    private Ingrediente queso, tocineta, cebolla;
    private ProductoAjustado pa;

    @BeforeEach
    void setUp() {
        base = new ProductoMenu("Hamburguesa Sencilla", 12000);
        queso = new Ingrediente("Queso", 2000);
        tocineta = new Ingrediente("Tocineta", 3000);
        cebolla = new Ingrediente("Cebolla", 500);
        pa = new ProductoAjustado(base);
    }

    @AfterEach
    void tearDown() {
        base = null;
        queso = null;
        tocineta = null;
        cebolla = null;
        pa = null;
    }

    @Test
    void getNombre_delegaAlBase() {
        assertEquals("Hamburguesa Sencilla", pa.getNombre());
    }

    @Test
    void getPrecio_sinAgregados_esBase() {
        assertEquals(12000, pa.getPrecio());
    }

    @Test
    void getPrecio_conAgregados_sumaCostos_eliminadosNoRestan() {
        pa.agregarIngrediente(queso);
        pa.agregarIngrediente(tocineta); 
        pa.eliminarIngrediente(cebolla);  
        assertEquals(12000 + 2000 + 3000, pa.getPrecio());
    }

    @Test
    void agregarIngredienteRepetido_sumaDosVeces() {
        pa.agregarIngrediente(queso);
        pa.agregarIngrediente(queso);
        assertEquals(12000 + 2000 + 2000, pa.getPrecio());
    }

    @Test
    void eliminarIngredienteMultiplesVeces_noCambiaPrecio() {
        pa.eliminarIngrediente(cebolla);
        pa.eliminarIngrediente(cebolla);
        assertEquals(12000, pa.getPrecio());
    }

    @Test
    void generarTextoFactura_contieneBase_agregados_eliminados_yTotal() {
        pa.agregarIngrediente(queso);
        pa.eliminarIngrediente(cebolla);
        String fac = pa.generarTextoFactura();

        assertTrue(fac.contains("Hamburguesa Sencilla"));
        assertTrue(fac.contains("Base: 12000"));
        assertTrue(fac.contains("+ Queso") || fac.contains("+Queso"));
        assertTrue(fac.contains("2000"));
        assertTrue(fac.contains("- Cebolla") || fac.contains("-Cebolla"));

        int esperado = 12000 + 2000;
        assertTrue(fac.contains("TOTAL: $" + esperado) || fac.contains("total: $" + esperado));
    }

    @Test
    void agregarNull_y_eliminarNull_noRevienta_niCambiaPrecio() {
        pa.agregarIngrediente(null);
        pa.eliminarIngrediente(null);
        assertEquals(12000, pa.getPrecio());
    }
}