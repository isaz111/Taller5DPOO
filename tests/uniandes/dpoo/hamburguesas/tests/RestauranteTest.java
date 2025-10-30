package uniandes.dpoo.hamburguesas.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import uniandes.dpoo.hamburguesas.excepciones.*;
import uniandes.dpoo.hamburguesas.mundo.Combo;
import uniandes.dpoo.hamburguesas.mundo.Pedido;
import uniandes.dpoo.hamburguesas.mundo.ProductoMenu;
import uniandes.dpoo.hamburguesas.mundo.Restaurante;

public class RestauranteTest {

    @TempDir
    Path tmp; // carpeta temporal 

    private Restaurante r;

    private File escribir(Path archivo, List<String> lineas) throws IOException {
        Files.write(archivo, lineas, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return archivo.toFile();
    }

    private File ingredientesOK(Path dir) throws IOException {
        return escribir(dir.resolve("ingredientes.txt"), List.of(
            "lechuga;1000",
            "tomate;1000",
            "cebolla;1000"
        ));
    }

    private File menuOK(Path dir) throws IOException {
        return escribir(dir.resolve("menu.txt"), List.of(
            "corral;14000",
            "papas medianas;5500",
            "gaseosa;5000"
        ));
    }

    private File combosOK(Path dir) throws IOException {
        return escribir(dir.resolve("combos.txt"), List.of(
            "combo corral;10%;corral;papas medianas;gaseosa"
        ));
    }

    @BeforeEach
    void setUp() {
        r = new Restaurante();
    }

    @AfterEach
    void tearDown() {
        r = null;
        try {
            Path facturas = Paths.get("facturas");
            if (Files.exists(facturas) && Files.isDirectory(facturas)) {
                try (DirectoryStream<Path> ds = Files.newDirectoryStream(facturas)) {
                    for (Path p : ds) Files.deleteIfExists(p);
                }
                Files.deleteIfExists(facturas);
            }
        } catch (IOException ignored) {}
    }

    // tamanos de colecciones esperados

    @Test
    void cargarInformacionRestaurante_pueblaColeccionesConArchivosValidos() throws Exception {
        File fIng = ingredientesOK(tmp);
        File fMenu = menuOK(tmp);
        File fComb = combosOK(tmp);

        r.cargarInformacionRestaurante(fIng, fMenu, fComb);

        assertEquals(3, r.getIngredientes().size(), "Debe cargar 3 ingredientes");
        assertEquals(3, r.getMenuBase().size(),     "Debe cargar 3 productos base");
        assertEquals(1, r.getMenuCombos().size(),   "Debe cargar 1 combo");

        // Verificaciones rápidas de contenido accesible
        ProductoMenu pm0 = r.getMenuBase().get(0);
        assertNotNull(pm0);
        Combo c0 = r.getMenuCombos().get(0);
        assertNotNull(c0);
        assertTrue(c0.getNombre().toLowerCase(Locale.ROOT).contains("combo corral"));
    }


    // Cargar dos veces
    
    @Test
    void cargarDosVeces_reemplazaColecciones_noAcumula() throws Exception {
        File fIng1 = ingredientesOK(tmp);
        File fMenu1 = menuOK(tmp);
        File fComb1 = combosOK(tmp);

        r.cargarInformacionRestaurante(fIng1, fMenu1, fComb1);
        assertEquals(3, r.getIngredientes().size());
        assertEquals(3, r.getMenuBase().size());
        assertEquals(1, r.getMenuCombos().size());

        // Segunda carga con diferentes tamaños
        File fIng2 = escribir(tmp.resolve("ingredientes2.txt"), List.of(
            "queso;2000"
        ));
        File fMenu2 = escribir(tmp.resolve("menu2.txt"), List.of(
            "corral;14000",
            "gaseosa;5000"
        ));
        File fComb2 = escribir(tmp.resolve("combos2.txt"), List.of(
            "combo basico;10%;corral;gaseosa"
        ));

        r.cargarInformacionRestaurante(fIng2, fMenu2, fComb2);
        assertEquals(1, r.getIngredientes().size(), "Debe reemplazar ingredientes");
        assertEquals(2, r.getMenuBase().size(),     "Debe reemplazar menú base");
        assertEquals(1, r.getMenuCombos().size(),   "Debe reemplazar combos");
    }

    // flujo de pedido
 
    @Test
    void flujoPedido_cierra_guarday_agregaHistorico() throws Exception {
      
        r.iniciarPedido("Sofía", "Bogotá");
        Pedido enCurso = r.getPedidoEnCurso();
        assertNotNull(enCurso, "Debe haber pedido en curso tras iniciar");

        int id = enCurso.getIdPedido();
        r.cerrarYGuardarPedido(); 
        assertNull(r.getPedidoEnCurso(), "Después de cerrar, no debe haber pedido en curso");
        assertEquals(1, r.getPedidos().size(), "Histórico debe tener 1 pedido");

        Path factura = Paths.get("facturas", "factura_" + id + ".txt");
        assertTrue(Files.exists(factura), "Debe haberse creado el archivo de factura");

        String contenido = Files.readString(factura);
        assertTrue(contenido.contains("Cliente: Sofía"));
        assertTrue(contenido.contains("Dirección: Bogotá"));
        assertTrue(contenido.toLowerCase(Locale.ROOT).contains("precio total: 0")); 
    }

    // no permite dos pedidos en curso

    @Test
    void iniciarPedido_lanzaSiYaHayPedidoEnCurso() throws Exception {
        r.iniciarPedido("Ana", "Calle 1");
        assertThrows(YaHayUnPedidoEnCursoException.class,
            () -> r.iniciarPedido("Beto", "Calle 2"),
            "Debe lanzar si ya hay un pedido en curso");
    }

    // Cerrar sin pedido en curso
 
    @Test
    void cerrarSinPedido_lanzaNoHayPedidoEnCurso() {
        assertThrows(NoHayPedidoEnCursoException.class,
            () -> r.cerrarYGuardarPedido(),
            "Cerrar sin pedido en curso debe lanzar NoHayPedidoEnCursoException");
    }


    // ingrediente repetido

    @Test
    void cargarIngredientes_repetido_lanzaExcepcion() throws Exception {
        File fIng = escribir(tmp.resolve("ingredientes.txt"), List.of(
            "tomate;1000",
            "tomate;1500" 
        ));
        File fMenu = menuOK(tmp);
        File fComb = combosOK(tmp);

        assertThrows(IngredienteRepetidoException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    // producto base repetido
 
    @Test
    void cargarMenu_repetido_lanzaExcepcion() throws Exception {
        File fIng = ingredientesOK(tmp);
        File fMenu = escribir(tmp.resolve("menu.txt"), List.of(
            "corral;14000",
            "corral;15000" 
        ));
        File fComb = combosOK(tmp);

        assertThrows(ProductoRepetidoException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    //  con nombre repetido

    @Test
    void cargarCombos_repetido_lanzaExcepcion() throws Exception {
        File fIng = ingredientesOK(tmp);
        File fMenu = menuOK(tmp);
        File fComb = escribir(tmp.resolve("combos.txt"), List.of(
            "combo corral;10%;corral;papas medianas;gaseosa",
            "combo corral;7%;corral;papas medianas;gaseosa" 
        ));

        assertThrows(ProductoRepetidoException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    // combo que referencia producto inexistente

    @Test
    void cargarCombos_productoFaltante_lanzaExcepcion() throws Exception {
        File fIng = ingredientesOK(tmp);
        File fMenu = menuOK(tmp);
        File fComb = escribir(tmp.resolve("combos.txt"), List.of(
            "combo raro;10%;producto_que_no_existe;papas medianas;gaseosa"
        ));

        assertThrows(ProductoFaltanteException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    // numeros mal formados → NumberFormatException

    @Test
    void cargarMenu_conNumeroInvalido_lanzaNumberFormat() throws Exception {
        File fIng = ingredientesOK(tmp);
        File fMenu = escribir(tmp.resolve("menu.txt"), List.of(
            "corral;14x00" // precio inválido
        ));
        File fComb = escribir(tmp.resolve("combos.txt"), List.of()); // combos vacío

        assertThrows(NumberFormatException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    @Test
    void cargarIngredientes_conNumeroInvalido_lanzaNumberFormat() throws Exception {
        File fIng = escribir(tmp.resolve("ingredientes.txt"), List.of(
            "tomate;1k00" // costo inválido
        ));
        File fMenu = menuOK(tmp);
        File fComb = combosOK(tmp);

        assertThrows(NumberFormatException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    // archivos inexistentes → IOException

    @Test
    void cargarConArchivoInexistente_lanzaIOException() throws Exception {
        File fIng = tmp.resolve("NO_EXISTE_ING.txt").toFile();
        File fMenu = menuOK(tmp);
        File fComb = combosOK(tmp);

        assertThrows(IOException.class,
            () -> r.cargarInformacionRestaurante(fIng, fMenu, fComb));
    }

    @Test
    void gettersNuncaNulos_yListasInicialmenteVacias() {
        assertNotNull(r.getIngredientes());
        assertNotNull(r.getMenuBase());
        assertNotNull(r.getMenuCombos());
        assertNotNull(r.getPedidos());
        assertTrue(r.getIngredientes().isEmpty());
        assertTrue(r.getMenuBase().isEmpty());
        assertTrue(r.getMenuCombos().isEmpty());
        assertTrue(r.getPedidos().isEmpty());
        assertNull(r.getPedidoEnCurso());
    }


    // descuento con decimales "9.5%"

    @Test
    void cargarComboConDescuentoDecimal_95_ok() throws Exception {
        File fIng = ingredientesOK(tmp);
        // menú con precios controlados
        File fMenu = escribir(tmp.resolve("menu.txt"), List.of(
            "especial;24000",
            "papas medianas;6000",
            "gaseosa;4000"
        ));
        File fComb = escribir(tmp.resolve("combos.txt"), List.of(
            "combo especial;9.5%;especial;papas medianas;gaseosa"
        ));

        r.cargarInformacionRestaurante(fIng, fMenu, fComb);

        assertEquals(1, r.getMenuCombos().size());
        Combo co = r.getMenuCombos().get(0);
        assertEquals("combo especial", co.getNombre());

        // precio esperado = (24000 + 6000 + 4000) * (1 - 0.095) = 34000 * 0.905 = 30770 → (int) = 30770
        int esperado = (int) (34000 * (1 - 0.095));
        assertEquals(esperado, co.getPrecio(), "El precio del combo con 9.5% debe calcularse correctamente");
    }
}
