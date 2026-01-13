import android.net.Uri
fun main() {
    val url = "https://vdiabete.com/resetpassword?password=0&token=TEST123"
    val uri = Uri.parse(url)
    println("Full URL: $url")
    println("Scheme: ${uri.scheme}")
    println("Host: ${uri.host}")
    println("Path: ${uri.path}")
    println("LastPathSegment: ${uri.lastPathSegment}")
    println("Token param: ${uri.getQueryParameter("token")}")
    println("Password param: ${uri.getQueryParameter("password")}")
    // Проверка логики из DynamicLinkNavigationMapper
    val token = uri.getQueryParameter("token")
    val lastSegment = uri.lastPathSegment
    println("\nПроверка условия:")
    println("token != null: ${token != null}")
    println("lastPathSegment: $lastSegment")
    println("equals('resetpassword', ignoreCase=true): ${lastSegment.equals("resetpassword", true)}")
}
