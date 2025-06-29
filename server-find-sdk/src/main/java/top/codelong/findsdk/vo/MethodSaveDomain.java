package top.codelong.findsdk.vo;

/**
 * 接口方法信息
 */
public class MethodSaveDomain {

    private String methodName;

    private String parameterType;

    private String url;

    private Integer isAuth;

    private Integer isHttp;

    private String httpType;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getIsAuth() {
        return isAuth;
    }

    public void setIsAuth(Integer isAuth) {
        this.isAuth = isAuth;
    }

    public Integer getIsHttp() {
        return isHttp;
    }

    public void setIsHttp(Integer isHttp) {
        this.isHttp = isHttp;
    }

    public String getHttpType() {
        return httpType;
    }

    public void setHttpType(String httpType) {
        this.httpType = httpType;
    }
}
