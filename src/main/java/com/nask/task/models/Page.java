package com.nask.task.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Page {

    private final int count = 82;
    @JsonIgnore
    private final int elementsPerPage = 10;
    private final int pages = (int) Math.ceil(this.count / (double) this.elementsPerPage);

    private final List<Person> elements = new ArrayList<>();

    public int getCount() {
        return count;
    }

    public int getElementsPerPage() {
        return elementsPerPage;
    }

    public int getPages() {
        return pages;
    }

    public List<Person> getElements() {
        return elements;
    }

    public void addElement(Person element) {
        this.elements.add(element);
    }

}
