import { useRef, useState } from "react";
import { Download, Paperclip, Trash2, Upload } from "lucide-react";
import { useTranslation } from "react-i18next";
import { getAccessToken } from "../../services/auth/authStorage";
import { deleteManagerOrderAttachment, getOrderAttachmentDownloadUrl, uploadManagerOrderAttachment } from "../../services/purchaseOrder/purchaseOrderService";

const MAX_SIZE = 2 * 1024 * 1024;
const TYPES = ["application/pdf", "image/jpeg", "image/png"];

function PurchaseOrderAttachments({ orderId, editable = false, attachments, onChange }) {
  const { t } = useTranslation("orders");
  const [items, setItems] = useState(() => attachments || []);
  const [label, setLabel] = useState("");
  const [file, setFile] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const inputRef = useRef(null);
  const update = (next) => { setItems(next); onChange?.(next); };
  const select = (event) => { const selected = event.target.files?.[0]; setError(""); if (!selected) return; if (!TYPES.includes(selected.type)) { setError(t("attachments.invalidType")); return; } if (selected.size > MAX_SIZE) { setError(t("attachments.tooLarge")); return; } setFile(selected); };
  const upload = async () => { if (!label.trim() || !file) { setError(t("attachments.labelRequired")); return; } if (items.length >= 2) { setError(t("attachments.limit")); return; } setBusy(true); setError(""); try { const created = await uploadManagerOrderAttachment(orderId, label.trim(), file); update([...items, created]); setLabel(""); setFile(null); if (inputRef.current) inputRef.current.value = ""; } catch (e) { setError(e.message || t("attachments.uploadError")); } finally { setBusy(false); } };
  const remove = async (attachment) => { setBusy(true); try { await deleteManagerOrderAttachment(orderId, attachment.id); update(items.filter((item) => item.id !== attachment.id)); } catch (e) { setError(e.message || t("attachments.deleteError")); } finally { setBusy(false); } };
  const download = async (attachment) => { const response = await fetch(getOrderAttachmentDownloadUrl(orderId, attachment.id), { headers: { Authorization: `Bearer ${getAccessToken()}` } }); if (!response.ok) return; const blob = await response.blob(); const url = URL.createObjectURL(blob); const anchor = document.createElement("a"); anchor.href = url; anchor.download = attachment.originalFilename; anchor.click(); URL.revokeObjectURL(url); };
  return <section className="order-attachments"><div className="order-attachments-heading"><div><p className="orders-eyebrow">{t("attachments.eyebrow")}</p><h2>{t("attachments.title")}</h2></div><span>{items.length}/2</span></div>{items.length ? <div className="order-attachments-list">{items.map((item) => <div className="order-attachment" key={item.id}><Paperclip size={16} /><div><strong>{item.displayName}</strong><small>{item.originalFilename} · {(item.fileSize / 1024 / 1024).toFixed(2)} MB</small></div><button type="button" onClick={() => download(item)} aria-label={t("attachments.download")}><Download size={15} /></button>{editable && <button type="button" onClick={() => remove(item)} disabled={busy} aria-label={t("attachments.remove")}><Trash2 size={15} /></button>}</div>)}</div> : <p className="orders-muted">{t("attachments.none")}</p>}{editable && items.length < 2 && <div className="order-attachments-form"><input value={label} onChange={(event) => setLabel(event.target.value)} placeholder={t("attachments.labelPlaceholder")} aria-label={t("attachments.label")} /><input ref={inputRef} type="file" accept="application/pdf,image/jpeg,image/png" onChange={select} /><button type="button" className="orders-secondary-button" onClick={upload} disabled={busy}><Upload size={15} />{t("attachments.add")}</button></div>}{error && <p className="orders-alert">{error}</p>}</section>;
}
export default PurchaseOrderAttachments;
