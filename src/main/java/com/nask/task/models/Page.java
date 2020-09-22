package com.nask.task.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Page {

    @JsonIgnore
    private final int elementsPerPage = 10;
    private int count;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int pages;
    private List<Person> elements = new ArrayList<>();

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.pages = (int) Math.ceil(count / (double) this.elementsPerPage);
        this.count = count;
    }

    public int getPages() {
        return pages;
    }

    // There was a jackson/spring issue with having setElements and getElements
    // while they have different JsonProperties which was recommended to fix by changing one of the method names
    @JsonProperty("results")
    public void setResults(List<Person> elements) {
        this.elements = elements;
    }

    @JsonProperty("elements")
    public List<Person> getElements() {
        return elements;
    }

    public void addElement(Person element) {
        this.elements.add(element);
    }

}
