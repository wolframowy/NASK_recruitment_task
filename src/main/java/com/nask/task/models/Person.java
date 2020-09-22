package com.nask.task.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Person {

    private String id;
    private String name;
    private String height;
    private String mass;
    private String hairColor;
    private String skinColor;
    private String eyeColor;
    private String birthYear;
    private String gender;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String homeworldUrl;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> starshipsUrls;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String url;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Planet homeworld;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final List<Starship> starships = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getMass() {
        return mass;
    }

    public void setMass(String mass) {
        this.mass = mass;
    }

    public String getHairColor() {
        return hairColor;
    }

    @JsonProperty("hair_color")
    public void setHairColor(String hairColor) {
        this.hairColor = hairColor;
    }

    public String getSkinColor() {
        return skinColor;
    }

    @JsonProperty("skin_color")
    public void setSkinColor(String skinColor) {
        this.skinColor = skinColor;
    }

    public String getEyeColor() {
        return eyeColor;
    }

    @JsonProperty("eye_color")
    public void setEyeColor(String eyeColor) {
        this.eyeColor = eyeColor;
    }

    public String getBirthYear() {
        return birthYear;
    }

    @JsonProperty("birth_year")
    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHomeworldUrl() {
        return homeworldUrl;
    }

    @JsonProperty("homeworld")
    public void setHomeworldUrl(String homeworldUrl) {
        this.homeworldUrl = homeworldUrl;
    }

    public List<String> getStarshipsUrls() {
        return starshipsUrls;
    }

    @JsonProperty("starships")
    public void setStarshipsUrls(List<String> starshipsUrls) {
        this.starshipsUrls = starshipsUrls;
    }

    public Planet getHomeworld() {
        return homeworld;
    }

    public void setHomeworld(Planet homeworld) {
        this.homeworld = homeworld;
    }

    public List<Starship> getStarships() {
        return starships;
    }

    public void addStarship(Starship starship) {
        this.starships.add(starship);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
