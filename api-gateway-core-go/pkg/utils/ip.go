package utils

import (
	"net"
	"strings"
)

// GetOutboundIP 根据目标地址获取本机用于通信的IP
// targetAddr 格式如 "192.168.1.1:80"
func GetOutboundIP(targetAddr string) (string, error) {
	conn, err := net.Dial("udp", targetAddr)
	if err != nil {
		return "", err
	}
	defer conn.Close()

	localAddr := conn.LocalAddr().(*net.UDPAddr)
	return localAddr.IP.String(), nil
}

// GetLocalIP 获取本机IP
// 优先返回私有地址，并尝试排除虚拟网卡
func GetLocalIP() string {
	interfaces, err := net.Interfaces()
	if err != nil {
		return "127.0.0.1"
	}

	var candidates []string

	for _, iface := range interfaces {
		// 排除 Docker, VMnet1 (Host-Only) 等常见虚拟或非主用网卡
		// 注意：VMnet8 通常是 NAT，可以访问外网，有时是需要的，所以不排除 VMnet8
		if isIgnoredInterface(iface.Name) {
			continue
		}

		addrs, err := iface.Addrs()
		if err != nil {
			continue
		}

		for _, addr := range addrs {
			if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
				if ipnet.IP.To4() != nil {
					ipStr := ipnet.IP.String()
					// 排除 169.254.x.x
					if strings.HasPrefix(ipStr, "169.254") {
						continue
					}
					candidates = append(candidates, ipStr)
				}
			}
		}
	}

	// 优先返回私有地址
	for _, ip := range candidates {
		if isPrivateIP(ip) {
			return ip
		}
	}

	if len(candidates) > 0 {
		return candidates[0]
	}

	return "127.0.0.1"
}

func isIgnoredInterface(name string) bool {
	lowerName := strings.ToLower(name)
	// 排除 docker0, cni, flannel 等容器网卡
	if strings.Contains(lowerName, "docker") || strings.Contains(lowerName, "cni") || strings.Contains(lowerName, "flannel") {
		return true
	}
	// 排除 VMnet1 (通常是 Host-Only，不通外网)
	// VMnet8 通常是 NAT，保留
	if strings.Contains(lowerName, "vmnet1") && !strings.Contains(lowerName, "vmnet8") {
		return true
	}
	return false
}

func isPrivateIP(ipStr string) bool {
	ip := net.ParseIP(ipStr)
	if ip == nil {
		return false
	}
	ip4 := ip.To4()
	if ip4 == nil {
		return false
	}

	// 10.0.0.0/8
	if ip4[0] == 10 {
		return true
	}
	// 172.16.0.0/12
	if ip4[0] == 172 && ip4[1] >= 16 && ip4[1] <= 31 {
		return true
	}
	// 192.168.0.0/16
	if ip4[0] == 192 && ip4[1] == 168 {
		return true
	}
	return false
}
