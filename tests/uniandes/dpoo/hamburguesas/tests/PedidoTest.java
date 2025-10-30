package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.Producto;

public class PedidoTest {

    private static class StubProducto implements Producto {
        private final String nombre;
        private final int precio;

        StubProducto(String nombre, int precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        @Override public String getNombre() { return nombre; }
        @Override public int getPrecio()   { return precio; }

        @Override
        public String generarTextoFactura() {
            return nombre + "\n" + precio + "\n";
        }
    }

    private Pedido pedido;

    @BeforeEach
    void setUp() {
        pedido = new Pedido("isabella", "Bogota");
    }

    @AfterEach
    void tearDown() {
        pedido = null;
    }

    @Test
    void constructorAsignaClienteYDireccion() {
        assertEquals("isabella", pedido.getNombreCliente(), "nombre de cliente incorrecto");
        assertTrue(pedido.getIdPedido() >= 0, "el id del pedido debe ser positivo");
    }

    @Test
    void idsIncrementanEntreInstancias() {
        Pedido p1 = new Pedido("A", "X");
        Pedido p2 = new Pedido("B", "Y");
        assertEquals(p1.getIdPedido() + 1, p2.getIdPedido(), "el id del segundo pedido debe ser consecutivo");
    }

    @Test
    void totalConIvaTresProductos() {
        pedido.agregarProducto(new StubProducto("hamb", 10000));
        pedido.agregarProducto(new StubProducto("papas", 6000));
        pedido.agregarProducto(new StubProducto("gaseosa", 4000));

        int neto = 10000 + 6000 + 4000;     
        int iva  = (int) (neto * 0.19);           
        int totalEsperado = neto + iva;     
        assertEquals(totalEsperado, pedido.getPrecioTotalPedido(), "total con IVA incorrecto");
    }

    @Test
    void pedidoVacioTotalesCeroYFacturaValida() {
        assertEquals(0, pedido.getPrecioTotalPedido(), "el pedido vacío debe costar 0");
        String facLower = pedido.generarTextoFactura().toLowerCase();

        assertTrue(facLower.contains("precio neto:  0"), "la factura vacía debe mostrar neto 0");
        assertTrue(facLower.contains("iva:          0"),  "la factura vacía debe mostrar IVA 0");
        assertTrue(facLower.contains("precio total: 0"),  "la factura vacía debe mostrar total 0");
    }

    @Test
    void generarTextoFacturaClienteItemsYTotales() {
        pedido.agregarProducto(new StubProducto("hamb", 10000));
        pedido.agregarProducto(new StubProducto("papas", 6000));

        int neto = 16000;
        int iva  = (int) (neto * 0.19);
        int total = neto + iva;

        String facturaLower = pedido.generarTextoFactura().toLowerCase();

        assertTrue(facturaLower.contains("cliente: isabella"), "debe incluir el cliente");
        assertTrue(facturaLower.contains("dirección: bogota"), "debe incluir la dirección");
        assertTrue(facturaLower.contains("hamb"),  "debe listar 'hamb'");
        assertTrue(facturaLower.contains("10000"), "debe listar precio de 'hamb'");
        assertTrue(facturaLower.contains("papas"), "debe listar 'papas'");
        assertTrue(facturaLower.contains("6000"),  "debe listar precio de 'papas'");
        assertTrue(facturaLower.contains("precio neto:  " + neto), "debe mostrar neto correcto");
        assertTrue(facturaLower.contains("iva:          " + iva),  "debe mostrar iva 19%");
        assertTrue(facturaLower.contains("precio total: " + total), "debe mostrar total correcto");
    }

    @Test
    void guardarFacturaArchivoConContenido(@TempDir File tmp) throws IOException {
        pedido.agregarProducto(new StubProducto("hamb", 10000));
        String esperado = pedido.generarTextoFactura();

        File archivo = new File(tmp, "factura-" + pedido.getIdPedido() + ".txt");

        assertDoesNotThrow(() -> pedido.guardarFactura(archivo), "no debe lanzar excepción al guardar");

        assertTrue(archivo.exists(), "el archivo de factura debe existir");
        String real = Files.readString(archivo.toPath());
        assertEquals(esperado, real, "el contenido del archivo debe coincidir exactamente con la factura");
    }
}
