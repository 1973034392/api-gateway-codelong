package top.codelong.findsdk.vo;

import java.util.List;

public class InterfaceRegisterVO {
    private String serverUrl;
    private String safeKey;
    private String safeSecret;
    private String interfaceName;
    private List<MethodSaveDomain> methods;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getSafeKey() {
        return safeKey;
    }

    public void setSafeKey(String safeKey) {
        this.safeKey = safeKey;
    }

    public String getSafeSecret() {
        return safeSecret;
    }

    public void setSafeSecret(String safeSecret) {
        this.safeSecret = safeSecret;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public List<MethodSaveDomain> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodSaveDomain> methods) {
        this.methods = methods;
    }
}
