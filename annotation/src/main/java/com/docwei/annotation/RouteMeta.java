package com.docwei.annotation;

import java.util.Objects;

import javax.lang.model.element.TypeElement;

public class RouteMeta {
    public String path;
    public String Group;
    public Class<?> destination;
    public TypeElement element;

    public RouteMeta(String path, String group, Class<?> destination) {
        this.path = path;
        Group = group;
        this.destination = destination;
    }

    public RouteMeta(String path, String group) {
        this.path = path;
        Group = group;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getGroup() {
        return Group;
    }

    public void setGroup(String group) {
        Group = group;
    }

    public Class<?> getDestination() {
        return destination;
    }

    public void setDestination(Class<?> destination) {
        this.destination = destination;
    }

    public TypeElement getElement() {
        return element;
    }

    public void setElement(TypeElement element) {
        this.element = element;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteMeta)) return false;
        RouteMeta routeMeta = (RouteMeta) o;
        return Objects.equals(getPath(), routeMeta.getPath()) &&
                Objects.equals(getGroup(), routeMeta.getGroup()) &&
                Objects.equals(element, routeMeta.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getGroup(), element);
    }
}
