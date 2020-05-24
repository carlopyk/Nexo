package net.azarquiel.nexo.Model

import java.io.Serializable

data class Alumno(var nombre:String="", var clase:String="", var asistencia:ArrayList<String>, var email:String="", var id:String=""):Serializable
data class Profesor(var nombre:String="", var email:String=""):Serializable
data class Asignatura(var nombre:String="", var clase:String, var codAsignatura: String):Serializable
data class Dia(var asignaturas:ArrayList<String>, var clase:ArrayList<String>, var horas:ArrayList<String>)
data class Trabajo(var actividad:String="", var descripcion:String="", var asignatura:String="", var fecha:String="", var terminado:Boolean=false, var tipo:String=""):Serializable
data class Aviso(var descripcion:String="", var asignatura:String="", var profesor:String=""):Serializable
data class Canjear(var puntos: Long = 0, var descripcion: String = "", var fecha: String = "", var codAsignatura: String):Serializable
data class Puntos(var puntos:Long=0, var puntosdesc:ArrayList<String>):Serializable