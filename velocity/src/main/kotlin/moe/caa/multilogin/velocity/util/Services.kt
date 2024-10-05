package moe.caa.multilogin.velocity.util

import moe.caa.multilogin.api.MultiLoginAPIProvider
import moe.caa.multilogin.api.exception.ServiceNotRegisteredException
import moe.caa.multilogin.api.service.BaseService

fun BaseService.requireRegistered(){
    if (!MultiLoginAPIProvider.api.serviceManager.getServices().contains(this)) {
        throw ServiceNotRegisteredException("Service is not registered!")
    }
}