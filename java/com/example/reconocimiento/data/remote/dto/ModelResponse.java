package com.example.reconocimiento.data.remote.dto;

public class ModelResponse {
    private String version;
    private String fecha;
    private String archivo;
    private String txt_archivo;

    public ModelResponse() {}

    public ModelResponse(String version, String fecha, String archivo, String txt_archivo) {
        this.version = version;
        this.fecha = fecha;
        this.archivo = archivo;
        this.txt_archivo = txt_archivo;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }

    public String getTxt_archivo() {
        return txt_archivo;
    }

    public void setTxt_archivo(String txt_archivo) {
        this.txt_archivo = txt_archivo;
    }
}