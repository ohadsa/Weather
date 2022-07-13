package gini.ohadsa.weather.utils.permissions

import androidx.fragment.app.Fragment

interface PermissionRequestHandler {

    fun rationale(description: String): PermissionRequestHandlerImpl
    fun request(vararg permission: Permission): PermissionRequestHandlerImpl
    fun checkPermission(callback: (Boolean) -> Unit)
    fun checkDetailedPermission(callback: (Map<Permission, Boolean>) -> Unit)
    fun from(fragment: Fragment): PermissionRequestHandlerImpl
}