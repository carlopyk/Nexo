package net.azarquiel.nexo.Model

interface InterfazOnLongClick {
    fun onLongClickTrabajo(trabajo: Trabajo): Boolean
}

interface InterfazOnLongClickCanjear {
    fun onLongClickCanjear(canjear: Canjear): Boolean
}