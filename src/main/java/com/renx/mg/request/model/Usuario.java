package com.renx.mg.request.model;

import java.io.Serializable;


/**
 * The persistent class for the usuario database table.
 * 
 */
// @Entity
// @NamedQuery(name="Usuario.findAll", query="SELECT u FROM Usuario u")
public class Usuario implements Serializable {
  private static final long serialVersionUID = 1L;

  // @Id
  private int idusuario;

  private String clave;

  private String idperfil;

  private String username;

  public Usuario() {}

  public int getIdusuario() {
    return this.idusuario;
  }

  public void setIdusuario(int idusuario) {
    this.idusuario = idusuario;
  }

  public String getClave() {
    return this.clave;
  }

  public void setClave(String clave) {
    this.clave = clave;
  }

  public String getIdperfil() {
    return this.idperfil;
  }

  public void setIdperfil(String idperfil) {
    this.idperfil = idperfil;
  }

  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

}
