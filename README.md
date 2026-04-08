# XNK Portal - Release va Deploy len k3s

- DockerHub account: `vdtry06`
- Deploy len cum k3s co san:
  - `xnk@192.168.199.201` (master + worker)
  - `xnk@192.168.199.202` (worker)
  - `xnk@192.168.199.203` (worker)

Tag release mac dinh da dat san:
- `v2026.04.08-r1`

Neu can release moi, ban tang tag theo mau:
- `v2026.04.08-r2`
- `v2026.04.09-r1`

---

## 1. Push image len DockerHub (tat ca services)

Chay tren may build (local):

```bash
cd xuatnhapkhau
docker login
bash scripts/build-push-dockerhub.sh
```

Lenh tren se mac dinh:
- user: `vdtry06`
- tag: `v2026.04.08-r1`
- push ca `:v2026.04.08-r1` va `:latest`

Neu muon tu dat tag:

```bash
bash scripts/build-push-dockerhub.sh vdtry06 v2026.04.08-r2
```

---

## 2. Push tung service rieng le

Script:
`scripts/build-push-one-service.sh`

Vi du push rieng order-service:

```bash
bash scripts/build-push-one-service.sh order-service v2026.04.08-r2 vdtry06
```

Cac service hop le:
- `auth-service`
- `api-gateway`
- `agent-service`
- `product-service`
- `supplier-service`
- `order-service`
- `payment-service`
- `frontend`

---

## 3. Copy project len server k3s va deploy

Tu may local copy source len master:

```bash
scp -r xuatnhapkhau xnk@192.168.199.201:/home/xnk/
```

Dang nhap master:

```bash
ssh xnk@192.168.199.201
cd /home/xnk/xuatnhapkhau
```

Deploy tu image DockerHub:

```bash
bash scripts/deploy-k3s-from-dockerhub.sh
```

Mac dinh script se dung:
- user: `vdtry06`
- tag: `v2026.04.08-r1`
- namespace: `default`

Neu muon chi dinh ro:

```bash
bash scripts/deploy-k3s-from-dockerhub.sh vdtry06 v2026.04.08-r2 default
```

---

## 4. Kiem tra trang thai sau deploy

```bash
kubectl get nodes -o wide
kubectl get pods -n default -o wide
kubectl get svc -n default
```

Neu can, cho rollout status:

```bash
kubectl rollout status deployment/auth-service -n default
kubectl rollout status deployment/api-gateway -n default
kubectl rollout status deployment/agent-service -n default
kubectl rollout status deployment/product-service -n default
kubectl rollout status deployment/supplier-service -n default
kubectl rollout status deployment/order-service -n default
kubectl rollout status deployment/payment-service -n default
kubectl rollout status deployment/frontend -n default
```

---

## 5. Logs tung service

Xem nhanh:

```bash
kubectl logs deployment/auth-service -n default --tail=200
kubectl logs deployment/api-gateway -n default --tail=200
kubectl logs deployment/agent-service -n default --tail=200
kubectl logs deployment/product-service -n default --tail=200
kubectl logs deployment/supplier-service -n default --tail=200
kubectl logs deployment/order-service -n default --tail=200
kubectl logs deployment/payment-service -n default --tail=200
kubectl logs deployment/frontend -n default --tail=200
kubectl logs deployment/mysql -n default --tail=200
```

Gom log ra file:

```bash
bash scripts/collect-k3s-logs.sh default 500 logs
```

Ket qua trong thu muc:
- `logs/<timestamp>/auth-service.log`
- `logs/<timestamp>/order-service.log`
- ...

---

## 6. Seed SQL cho nhieu agent/supplier + supplier inventory

File seed:
- `docs/preorder-seed.sql`

File da co san:
- nhieu agent (`agent01..agent04`)
- nhieu supplier (`supplier01..supplier04`)
- du lieu `supplier_inventory` de test flow nhieu supplier nhan cung order

Import seed (vi du voi mysql container docker):

```bash
docker exec -i xnk-mysql mysql -uroot -p<ROOT_PASSWORD> < docs/preorder-seed.sql
```

---

## 7. Nghiệp vụ UI hien tai

- Admin chi co:
  - `/admin/agents`
  - `/admin/suppliers`
- Agent:
  - dat hang online
  - xem don
  - chon supplier theo tung line
  - tao payment
  - xac nhan delivered
- Supplier:
  - nhan don theo tung line
  - cap nhat payment status (SUCCESS/FAILED)
  - cap nhat SHIPPED

---

## 8. Ghi chu van hanh

- Java images da duoc toi uu nhe hon (distroless runtime).
- Moi service co `.dockerignore` de giam build context.
- Neu UI/API chua cap nhat sau deploy, kiem tra:
  1. image tag dang chay (`kubectl describe pod ...`)
  2. rollout restart deployment tuong ung
  3. browser hard refresh / clear cache frontend
