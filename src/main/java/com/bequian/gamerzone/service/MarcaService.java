package com.bequian.gamerzone.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bequian.gamerzone.model.Marca;

@Service
public class MarcaService {

    // Marcas falsas simuladas.
    private final List<Marca> marcas = List.of(
        new Marca("HexaGear", "Periféricos",
            "Teclados mecánicos y mouses de precisión para competir.", "15% dcto. con código GZ15"),
        new Marca("PixelFuel", "Bebidas energéticas",
            "La energía oficial de las LAN Party GamerZone.", "2x1 en eventos presenciales"),
        new Marca("NovaNet", "Internet fibra",
            "Fibra óptica con baja latencia para que nunca pierdas una partida.", "3 meses a mitad de precio"),
        new Marca("Arcadia", "Sillas gamer",
            "Comodidad para maratones de juego de muchas horas.", "Despacho gratis a todo Chile")
    );

    public List<Marca> listarTodas() {
        return marcas;
    }
}