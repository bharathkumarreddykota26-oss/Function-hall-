const halls=[
["Royal Grand Hall","Plain hall • 800 guests","₹45,000","🏛️"],["Crystal Palace","AC hall • 500 guests","₹38,000","💎"],["Green Garden Hall","Garden venue • 700 guests","₹55,000","🌿"],["Pearl Banquet","Premium hall • 350 guests","₹30,000","🏰"],["Lakshmi Convention","Large hall • 1200 guests","₹70,000","🏛️"],["Sunrise Mini Hall","Small function • 200 guests","₹18,000","🌅"],["Golden Crown Hall","Wedding hall • 600 guests","₹52,000","👑"],["Lotus Celebration Hall","AC • 450 guests","₹42,000","🪷"]];
const chairs=[["Plastic Chair Pack 100","100 chairs","₹4,000","🪑"],["Banquet Chair Pack 100","100 padded chairs","₹7,500","💺"],["Royal Chair Pack 100","Cushioned premium chairs","₹12,000","👑"],["Chiavari Chair Pack 100","Elegant wedding chairs","₹15,000","✨"],["White Cover Chair 100","White covers included","₹8,000","🤍"],["Gold Chair 100","Gold finish chairs","₹14,000","🌟"],["VIP Sofa Seating","10 sofa sets","₹18,000","🛋️"],["Kids Chair Pack 50","50 colorful chairs","₹2,500","🧒"]];
const decor=[["Classic Stage Decor","Backdrop + flowers + lights","₹18,000","🌸"],["Royal Wedding Decor","Premium stage + floral setup","₹35,000","👑"],["Floral Entrance","Fresh-style floral entrance","₹12,000","🌺"],["LED Stage Package","LED wall + stage lights","₹28,000","💡"],["Mandap Decoration","Traditional mandap setup","₹25,000","🛕"],["Reception Backdrop","Elegant reception backdrop","₹14,000","✨"],["Balloon Decoration","Theme balloon package","₹8,000","🎈"],["Premium Table Decor","Centerpieces + table styling","₹16,000","🍽️"]];
const vegNames=["Paneer Butter Masala","Kadai Paneer","Palak Paneer","Shahi Paneer","Veg Biryani","Jeera Rice","Gobi Manchurian","Veg Noodles","Masala Dosa","Idli Sambar","Medu Vada","Chole Bhature","Dal Tadka","Dal Makhani","Aloo Gobi","Veg Kurma","Mushroom Masala","Baby Corn Fry","Gobi 65","Vegetable Pulao","Curd Rice","Rasam","Sambar","Chapati"];
const nonVegNames=["Chicken Biryani","Mutton Biryani","Chicken 65","Chicken Tikka","Butter Chicken","Chicken Curry","Mutton Curry","Mutton Fry","Fish Fry","Fish Curry","Prawn Masala","Prawn Fry","Chicken Kebab","Chicken Lollipop","Tandoori Chicken","Egg Curry","Egg Biryani","Pepper Chicken","Chilli Chicken","Chicken Manchurian","Mutton Biryani Special","Fish Tikka","Chicken Chettinad","Mutton Korma"];
const icons=["🍛","🍲","🥘","🍚","🍜","🥗","🍽️","🌶️"];
const veg=vegNames.map((n,i)=>[n,"Catering portion • vegetarian","₹"+(120+i*5),icons[i%icons.length]]);
const nonveg=nonVegNames.map((n,i)=>[n,"Catering portion • non-vegetarian","₹"+(180+i*8),["🍗","🍖","🐟","🍤"][i%4]]);

let cart=JSON.parse(localStorage.getItem("hallbookCart")||"[]");
function money(v){return "₹"+Number(v).toLocaleString("en-IN")}
function save(){localStorage.setItem("hallbookCart",JSON.stringify(cart));updateCount();renderCart()}
function updateCount(){const e=document.getElementById("cartCount");if(e)e.textContent=cart.reduce((s,x)=>s+x.qty,0)}
function addItem(name,price,icon){const p=cart.find(x=>x.name===name);if(p)p.qty++;else cart.push({name,price:Number(String(price).replace(/[^0-9.]/g,"")),qty:1,icon});save();toast("Added "+name)}
function removeItem(i){const row=document.querySelectorAll(".cart-row")[i];if(row)row.classList.add("removing");setTimeout(()=>{cart.splice(i,1);save()},280)}
function changeQty(i,d){cart[i].qty+=d;if(cart[i].qty<=0)return removeItem(i);save()}
function toast(t){const x=document.createElement("div");x.className="toast";x.textContent=t;Object.assign(x.style,{position:"fixed",right:"20px",bottom:"20px",background:"#222",color:"#fff",padding:"12px 18px",borderRadius:"10px",zIndex:20});document.body.appendChild(x);setTimeout(()=>x.remove(),1300)}
function cards(data,id){const el=document.getElementById(id);if(!el)return;el.innerHTML=data.map((p,i)=>`<article class="product"><div class="pic">${p[3]}</div><div class="product-body"><h3>${p[0]}</h3><p>${p[1]}</p><span class="price">${p[2]}</span><button class="add" onclick="addItem('${p[0].replace(/'/g,"\\'")}','${p[2]}','${p[3]}')">+ Add to Cart</button></div></article>`).join("")}
function showFood(type,btn){document.querySelectorAll(".tab").forEach(x=>x.classList.remove("active"));if(btn)btn.classList.add("active");cards(type==="veg"?veg:nonveg,"foodGrid")}
function renderCart(){const el=document.getElementById("cartItems");if(!el)return;if(!cart.length){el.innerHTML='<p class="empty">Your cart is empty. Add a hall, chairs, decoration or food item.</p>';const t=document.getElementById("total");if(t)t.textContent="₹0";return}el.innerHTML=cart.map((x,i)=>`<div class="cart-row"><span>${x.icon} <b>${x.name}</b><br><small>${money(x.price)} × ${x.qty}</small></span><span class="qty"><button onclick="changeQty(${i},-1)">−</button>${x.qty}<button onclick="changeQty(${i},1)">+</button><button class="remove" onclick="removeItem(${i})">Remove</button></span></div>`).join("");const total=cart.reduce((s,x)=>s+x.price*x.qty,0);document.getElementById("total").textContent=money(total)}
async function placeOrder(){const msg=document.getElementById("orderMsg");if(!cart.length){msg.className="message err";msg.textContent="Add at least one item.";return}const name=document.getElementById("customerName").value.trim(),phone=document.getElementById("phone").value.trim(),date=document.getElementById("eventDate").value,payment=document.getElementById("payment").value;if(!name||!phone||!date){msg.className="message err";msg.textContent="Please fill name, phone and function date.";return}const total=cart.reduce((s,x)=>s+x.price*x.qty,0);const payload={customerName:name,phone,eventDate:date,payment,total,items:cart};try{const r=await fetch("http://localhost:8080/api/bookings",{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(payload)});const data=await r.json();if(!r.ok)throw new Error(data.message||"Server error");msg.className="message ok";msg.textContent="Booking confirmed! Booking ID: "+data.bookingId;cart=[];save()}catch(e){msg.className="message err";msg.textContent="Demo saved locally. Start Java JDBC server for database booking. "+e.message}}
document.addEventListener("DOMContentLoaded",()=>{updateCount();cards(halls,"hallGrid");cards(chairs,"chairGrid");cards(decor,"decorGrid");if(document.getElementById("foodGrid"))showFood("veg",document.querySelector(".tab"));renderCart()});

// Optional JDBC API integration: call loadDatabaseCatalog() after Java server starts.
// The original static catalog remains as a frontend fallback.
async function loadDatabaseCatalog(){
  try{
    const [h,c,d] = await Promise.all([
      fetch("http://localhost:8080/api/halls").then(r=>r.json()),
      fetch("http://localhost:8080/api/chairs").then(r=>r.json()),
      fetch("http://localhost:8080/api/decorations").then(r=>r.json())
    ]);
    if(Array.isArray(h) && document.getElementById("hallGrid"))
      cards(h.map(x=>[x.name,x.description+" • "+x.capacity+" guests",money(x.price),"🏛️"]),"hallGrid");
    if(Array.isArray(c) && document.getElementById("chairGrid"))
      cards(c.map(x=>[x.name,x.description,money(x.price),"🪑"]),"chairGrid");
    if(Array.isArray(d) && document.getElementById("decorGrid"))
      cards(d.map(x=>[x.name,x.description,money(x.price),"🌸"]),"decorGrid");
    if(document.getElementById("foodGrid")){
      const type=document.querySelector(".tab.active")?.textContent.includes("Non")?"NON_VEG":"VEG";
      const data=await fetch("http://localhost:8080/api/food?type="+type).then(r=>r.json());
      if(Array.isArray(data)) cards(data.map(x=>[x.name,x.description,money(x.price),type==="VEG"?"🥗":"🍗"]),"foodGrid");
    }
  }catch(e){ /* frontend fallback data remains visible */ }
}
document.addEventListener("DOMContentLoaded",()=>setTimeout(loadDatabaseCatalog,200));
