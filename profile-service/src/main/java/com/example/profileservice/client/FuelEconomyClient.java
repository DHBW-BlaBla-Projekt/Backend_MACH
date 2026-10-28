package com.example.profileservice.client;

import com.example.profileservice.dto.VehicleDetailsDto;
import com.example.profileservice.dto.VehicleMenuItemDto;
import com.example.profileservice.dto.VehicleOptionDto;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;

@Component
public class FuelEconomyClient {
    private final RestClient restClient;
    private final String baseUrl;

    public FuelEconomyClient(
            @Value("${app.fuel-economy.base-url:https://www.fueleconomy.gov/ws/rest}")
            String baseUrl) {
        this.baseUrl = baseUrl.replaceAll("/+$", "");
        this.restClient = RestClient.builder().build();
    }

    public List<VehicleMenuItemDto> getYears() {
        return parseMenuItems(get("/vehicle/menu/year"));
    }

    public List<VehicleMenuItemDto> getMakes(int year) {
        return parseMenuItems(get("/vehicle/menu/make?year=" + year));
    }

    public List<VehicleMenuItemDto> getModels(int year, String make) {
        return parseMenuItems(get("/vehicle/menu/model?year=" + year + "&make=" + encode(make)));
    }

    public List<VehicleOptionDto> getOptions(
            int year,
            String make,
            String model) {
        Document document = parse(get("/vehicle/menu/options?year=" + year
                + "&make=" + encode(make) + "&model=" + encode(model)));
        NodeList items = document.getElementsByTagName("menuItem");
        List<VehicleOptionDto> options = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            String value = childText(item, "value");
            String label = childText(item, "text");
            if (!value.isBlank() && !label.isBlank()) {
                options.add(new VehicleOptionDto(Long.parseLong(value), label));
            }
        }
        return List.copyOf(options);
    }

    public VehicleDetailsDto getVehicleDetails(long vehicleId) {
        Document document = parse(get("/vehicle/" + vehicleId));
        Node vehicle = document.getDocumentElement();
        VehicleDetailsDto details = new VehicleDetailsDto();
        details.setVehicleId(vehicleId);
        details.setYear(integerText(vehicle, "year"));
        details.setMake(childText(vehicle, "make"));
        details.setModel(childText(vehicle, "model"));
        details.setCityMpg(integerText(vehicle, "city08"));
        details.setHighwayMpg(integerText(vehicle, "highway08"));
        details.setCombinedMpg(integerText(vehicle, "comb08"));
        Integer combinedMpg = details.getCombinedMpg();
        if (combinedMpg != null && combinedMpg > 0) {
            details.setConsumptionLPer100Km(235.214583 / combinedMpg);
        }
        details.setFuelType(childText(vehicle, "fuelType"));
        return details;
    }

    private String get(String path) {
        return restClient.get().uri(baseUrl + path).retrieve().body(String.class);
    }

    private List<VehicleMenuItemDto> parseMenuItems(String body) {
        Document document = parse(body);
        NodeList items = document.getElementsByTagName("menuItem");
        List<VehicleMenuItemDto> result = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            Node item = items.item(i);
            String value = childText(item, "value");
            String text = childText(item, "text");
            if (!value.isBlank() && !text.isBlank()) {
                result.add(new VehicleMenuItemDto(value, text));
            }
        }
        return List.copyOf(result);
    }

    private Document parse(String body) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Fuel-Economy-API response konnte nicht gelesen werden.", exception);
        }
    }

    private String childText(Node parent, String name) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (name.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return "";
    }

    private Integer integerText(Node parent, String name) {
        String value = childText(parent, name);
        if (value.isBlank() || "0".equals(value)) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("Ungültiger Zahlenwert '" + value + "' für " + name + ".", exception);
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
